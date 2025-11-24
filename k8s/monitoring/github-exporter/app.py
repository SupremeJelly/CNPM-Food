import os
import time
import threading
import requests
from prometheus_client import start_http_server, Gauge, Counter, Histogram
import io
import zipfile
import re

G_UP = Gauge('github_exporter_up', 'GitHub API up (1 = ok, 0 = failed)')
G_RATE_REMAIN = Gauge('github_rate_limit_remaining', 'GitHub rate limit remaining')
C_CHECKS = Counter('github_exporter_checks_total', 'Number of GitHub checks performed')
# Metrics for dashboard panels
REQ_TOTAL = Counter('github_requests_total', 'Total GitHub API requests')
REQ_ERRORS = Counter('github_requests_errors_total', 'Total GitHub API request errors')
# Histogram for request duration (buckets chosen for typical API latencies)
REQ_DURATION = Histogram('github_request_duration_seconds', 'GitHub request duration in seconds', buckets=[0.005,0.01,0.025,0.05,0.1,0.25,0.5,1,2.5,5,10])

# Workflow failure metrics
# Gauge set to 1 when the latest run for a workflow has a failing job (labels: workflow, job)
G_WORKFLOW_FAILED = Gauge('github_workflow_failed_job', '1 when latest run failed and this job failed', ['workflow', 'job'])
# Timestamp of the last failed run for a workflow (seconds since epoch)
G_WORKFLOW_LAST_FAILED_TS = Gauge('github_workflow_last_failed_timestamp_seconds', 'Unix timestamp of the last failed run for a workflow', ['workflow'])
# Counter for Loki pushes
LOKI_PUSHES = Counter('github_loki_pushes_total', 'Total number of pushes made to Loki')
# Testcase metrics
TESTCASE_TOTAL = Counter('github_workflow_testcase_total', 'Total testcases observed for a failing job', ['workflow', 'job'])
TESTCASE_FAILED = Counter('github_workflow_testcase_failed_total', 'Failed testcase occurrences', ['workflow', 'job', 'testcase'])
# Additional metrics for better visibility
G_TOTAL_FAILING_WORKFLOWS = Gauge('github_total_failing_workflows', 'Total number of workflows with failures')
G_TOTAL_FAILING_JOBS = Gauge('github_total_failing_jobs', 'Total number of failing jobs across all workflows')
STEP_FAILED = Counter('github_workflow_step_failed_total', 'Failed step occurrences', ['workflow', 'job', 'step'])
GITHUB_TOKEN = os.environ.get('GITHUB_TOKEN')
GITHUB_REPO = os.environ.get('GITHUB_REPO')  # expected form: owner/repo
LOKI_PUSH_URL = os.environ.get('LOKI_PUSH_URL')
LOKI_AUTH_HEADER = os.environ.get('LOKI_AUTH_HEADER')
POLL_INTERVAL = int(os.environ.get('POLL_INTERVAL', '30'))
PORT = int(os.environ.get('METRICS_PORT', '9118'))

HEADERS = {'Accept': 'application/vnd.github.v3+json'}
if GITHUB_TOKEN:
    HEADERS['Authorization'] = f'token {GITHUB_TOKEN}'


def poll_loop():
    # small cache to track previously-exposed failed (workflow,job) tuples
    prev_failed_jobs = set()
    print("Entering poll loop...")

    while True:
        try:
            print(f"[{time.strftime('%H:%M:%S')}] Polling GitHub API...")
            start = time.time()
            r = requests.get('https://api.github.com/rate_limit', headers=HEADERS, timeout=10)
            duration = time.time() - start
            # observe duration and counters
            REQ_DURATION.observe(duration)
            REQ_TOTAL.inc()
            C_CHECKS.inc()
            if r.status_code == 200:
                G_UP.set(1)
                data = r.json()
                # core.rate.remaining
                try:
                    remaining = data.get('resources', {}).get('core', {}).get('remaining')
                    if remaining is not None:
                        G_RATE_REMAIN.set(remaining)
                except Exception:
                    pass
            else:
                G_UP.set(0)
                REQ_ERRORS.inc()
            # --- minimal workflow failure polling ---
            # Only run if GITHUB_REPO is configured
            if GITHUB_REPO:
                try:
                    owner_repo = GITHUB_REPO.strip()
                    # fetch workflows
                    wf_url = f'https://api.github.com/repos/{owner_repo}/actions/workflows'
                    start_wf = time.time()
                    rw = requests.get(wf_url, headers=HEADERS, timeout=10)
                    REQ_DURATION.observe(time.time() - start_wf)
                    REQ_TOTAL.inc()
                    current_failed = set()
                    if rw.status_code == 200:
                        wfs = rw.json().get('workflows', [])
                        for wf in wfs:
                            wf_id = wf.get('id')
                            wf_name = wf.get('name') or str(wf_id)
                            # get latest runs (1)
                            runs_url = f'https://api.github.com/repos/{owner_repo}/actions/workflows/{wf_id}/runs?per_page=1'
                            rs = requests.get(runs_url, headers=HEADERS, timeout=10)
                            REQ_DURATION.observe(0)  # quick marker (we already measured above)
                            REQ_TOTAL.inc()
                            if rs.status_code != 200:
                                continue
                            runs = rs.json().get('workflow_runs', [])
                            if not runs:
                                continue
                            latest = runs[0]
                            conclusion = latest.get('conclusion')
                            run_id = latest.get('id')
                            if conclusion == 'failure' and run_id:
                                # record timestamp
                                ts = latest.get('updated_at') or latest.get('run_started_at')
                                try:
                                    # parse ISO 8601-ish timestamp to epoch
                                    t = time.strptime(ts.replace('Z', ''), '%Y-%m-%dT%H:%M:%S')
                                    epoch = time.mktime(t)
                                except Exception:
                                    epoch = time.time()
                                G_WORKFLOW_LAST_FAILED_TS.labels(workflow=wf_name).set(epoch)
                                # fetch jobs for the run to find failing job names
                                jobs_url = f'https://api.github.com/repos/{owner_repo}/actions/runs/{run_id}/jobs'
                                jj = requests.get(jobs_url, headers=HEADERS, timeout=10)
                                REQ_DURATION.observe(0)
                                REQ_TOTAL.inc()
                                if jj.status_code == 200:
                                    jobs = jj.json().get('jobs', [])
                                    for job in jobs:
                                        if job.get('conclusion') != 'success':
                                                job_name = job.get('name') or str(job.get('id'))
                                                current_failed.add((wf_name, job_name))
                                                # Inline: push a structured summary to Loki if configured
                                                try:
                                                    if LOKI_PUSH_URL:
                                                        run_url = f"https://github.com/{owner_repo}/actions/runs/{run_id}"
                                                        # build list of messages: job header + failing steps (if present)
                                                        messages = []
                                                        header = f"FAILED: workflow={wf_name} job={job_name} run={run_url} repo={owner_repo}"
                                                        messages.append(header)
                                                        # include failing steps if the jobs API provided them
                                                        for step in job.get('steps', []):
                                                            if step.get('conclusion') and step.get('conclusion') != 'success':
                                                                step_name = step.get('name') or str(step.get('number'))
                                                                step_conclusion = step.get('conclusion')
                                                                messages.append(f"  step={step_name} conclusion={step_conclusion}")
                                                                # Track failed steps
                                                                try:
                                                                    STEP_FAILED.labels(workflow=wf_name, job=job_name, step=step_name).inc()
                                                                except Exception:
                                                                    pass
                                                        # If job does not include steps, include link to run logs
                                                        if len(messages) == 1:
                                                            messages.append(f"  (see run logs at {run_url})")

                                                        # attempt to fetch job logs (zip) to extract failing testcases
                                                        try:
                                                            job_id = job.get('id')
                                                            if job_id:
                                                                logs_url = f'https://api.github.com/repos/{owner_repo}/actions/jobs/{job_id}/logs'
                                                                lr = requests.get(logs_url, headers=HEADERS, timeout=60)
                                                                if lr.status_code == 200:
                                                                    bio = io.BytesIO(lr.content)
                                                                    try:
                                                                        with zipfile.ZipFile(bio) as z:
                                                                            test_failures = []
                                                                            test_stats = {'total': 0, 'passed': 0, 'failed': 0, 'errors': 0}
                                                                            
                                                                            for fname in z.namelist():
                                                                                try:
                                                                                    with z.open(fname) as f:
                                                                                        text = f.read().decode('utf-8', errors='ignore')
                                                                                        
                                                                                        # Parse structured format (TEST_RUN, TEST_STATUS, TEST_ERROR)
                                                                                        lines = text.split('\n')
                                                                                        current_test = None
                                                                                        current_class = None
                                                                                        
                                                                                        for i, line in enumerate(lines):
                                                                                            line = line.strip()
                                                                                            
                                                                                            # Parse test totals
                                                                                            if line.startswith('TEST_TOTAL:'):
                                                                                                try:
                                                                                                    test_stats['total'] = int(line.split(':')[1].strip())
                                                                                                except:
                                                                                                    pass
                                                                                            
                                                                                            # Parse test run
                                                                                            if line.startswith('TEST_RUN:'):
                                                                                                current_test = line.split(':', 1)[1].strip()
                                                                                            
                                                                                            # Parse test class
                                                                                            if line.startswith('TEST_CLASS:'):
                                                                                                current_class = line.split(':', 1)[1].strip()
                                                                                            
                                                                                            # Parse test status
                                                                                            if line.startswith('TEST_STATUS:'):
                                                                                                status = line.split(':', 1)[1].strip()
                                                                                                
                                                                                                if status in ['FAIL', 'ERROR'] and current_test:
                                                                                                    # Get error message from next line if exists
                                                                                                    error_msg = ""
                                                                                                    if i + 1 < len(lines) and lines[i + 1].strip().startswith('TEST_ERROR:'):
                                                                                                        error_msg = lines[i + 1].split(':', 1)[1].strip()
                                                                                                    
                                                                                                    # Format test name
                                                                                                    if current_class:
                                                                                                        test_name = f"{current_test} ({current_class})"
                                                                                                    else:
                                                                                                        test_name = current_test
                                                                                                    
                                                                                                    # Add to failures if not duplicate
                                                                                                    if test_name not in test_failures:
                                                                                                        test_failures.append(test_name)
                                                                                                    
                                                                                                    if status == 'FAIL':
                                                                                                        test_stats['failed'] += 1
                                                                                                    elif status == 'ERROR':
                                                                                                        test_stats['errors'] += 1
                                                                                                
                                                                                                elif status == 'PASS':
                                                                                                    test_stats['passed'] += 1
                                                                                                
                                                                                                current_test = None
                                                                                                current_class = None
                                                                                            
                                                                                            # Parse summary stats
                                                                                            if line.startswith('TEST_PASSED:'):
                                                                                                try:
                                                                                                    test_stats['passed'] = int(line.split(':')[1].strip())
                                                                                                except:
                                                                                                    pass
                                                                                            if line.startswith('TEST_FAILED:'):
                                                                                                try:
                                                                                                    test_stats['failed'] = int(line.split(':')[1].strip())
                                                                                                except:
                                                                                                    pass
                                                                                            if line.startswith('TEST_ERRORS:'):
                                                                                                try:
                                                                                                    test_stats['errors'] = int(line.split(':')[1].strip())
                                                                                                except:
                                                                                                    pass
                                                                                        
                                                                                        # Fallback: Parse old format if no structured data found
                                                                                        if not test_failures:
                                                                                            old_pattern = re.compile(r'(?:FAIL:|ERROR:|FAILED)\s*[:\s\-]*([A-Za-z0-9_\.\-:\/\(\)]+)', re.IGNORECASE)
                                                                                            for match in old_pattern.finditer(text):
                                                                                                test_name = match.group(1)
                                                                                                if test_name and test_name not in test_failures and len(test_name) > 3:
                                                                                                    test_failures.append(test_name)
                                                                                
                                                                                except Exception:
                                                                                    pass
                                                                            # update metrics and also append to messages
                                                                            if test_failures or test_stats['total'] > 0:
                                                                                # Add test summary to messages
                                                                                if test_stats['total'] > 0:
                                                                                    summary = f"TEST_SUMMARY: Total={test_stats['total']} Passed={test_stats['passed']} Failed={test_stats['failed']} Errors={test_stats['errors']}"
                                                                                    messages.append(summary)
                                                                                
<<<<<<< HEAD
                                                                                # total observed
                                                                                try:
                                                                                    TESTCASE_TOTAL.labels(workflow=wf_name, job=job_name).inc(len(test_failures))
=======
                                                                                # total observed - use test_stats['total'] if available, otherwise count from failures
                                                                                try:
                                                                                    total_tests = test_stats['total'] if test_stats['total'] > 0 else (test_stats['passed'] + test_stats['failed'] + test_stats['errors'])
                                                                                    if total_tests > 0:
                                                                                        TESTCASE_TOTAL.labels(workflow=wf_name, job=job_name).inc(total_tests)
>>>>>>> c336b68ab1e4d9da9eb4005d36f9da7fde657e8b
                                                                                except Exception:
                                                                                    pass
                                                                                
                                                                                # Add individual failing tests
                                                                                for tc in test_failures:
                                                                                    try:
                                                                                        TESTCASE_FAILED.labels(workflow=wf_name, job=job_name, testcase=tc).inc()
                                                                                    except Exception:
                                                                                        pass
                                                                                    messages.append(f"TEST_FAIL: {tc}")
                                                                    except zipfile.BadZipFile:
                                                                        pass
                                                        except Exception:
                                                            pass

                                                        # prepare Loki payload with one stream and multiple values (each a line)
                                                        ts_ns = str(int(time.time() * 1e9))
                                                        stream = {
                                                            "stream": {
                                                                "app": "github-exporter",
                                                                "repo": owner_repo,
                                                                "workflow": wf_name,
                                                                "run_id": str(run_id),
                                                                "job": job_name
                                                            },
                                                            "values": [[ts_ns, msg] for msg in messages]
                                                        }
                                                        payload = {"streams": [stream]}
                                                        headers_loki = {"Content-Type": "application/json"}
                                                        if LOKI_AUTH_HEADER:
                                                            headers_loki["Authorization"] = LOKI_AUTH_HEADER
                                                        try:
                                                            rpush = requests.post(LOKI_PUSH_URL, json=payload, headers=headers_loki, timeout=10)
                                                            # increment counter on (attempt) — successful or not, we attempted a push
                                                            try:
                                                                LOKI_PUSHES.inc()
                                                            except Exception:
                                                                pass
                                                        except Exception:
                                                            pass
                                                except Exception:
                                                    pass
                    # update gauges: clear previously failing jobs that are no longer failing
                    to_clear = prev_failed_jobs - current_failed
                    for wf_name, job_name in to_clear:
                        try:
                            G_WORKFLOW_FAILED.labels(workflow=wf_name, job=job_name).set(0)
                        except Exception:
                            pass
                    # set current failing jobs to 1
                    for wf_name, job_name in current_failed:
                        try:
                            G_WORKFLOW_FAILED.labels(workflow=wf_name, job=job_name).set(1)
                        except Exception:
                            pass
                    prev_failed_jobs = current_failed
                    
                    # Update summary gauges
                    try:
                        unique_workflows = set(wf for wf, _ in current_failed)
                        G_TOTAL_FAILING_WORKFLOWS.set(len(unique_workflows))
                        G_TOTAL_FAILING_JOBS.set(len(current_failed))
                    except Exception:
                        pass
                except Exception:
                    # don't let workflow polling break the main loop
                    pass
        except Exception:
            G_UP.set(0)
            REQ_ERRORS.inc()
        time.sleep(POLL_INTERVAL)


if __name__ == '__main__':
    print(f"Starting GitHub Exporter on port {PORT}")
    print(f"GITHUB_TOKEN configured: {'Yes' if GITHUB_TOKEN else 'No'}")
    print(f"GITHUB_REPO: {GITHUB_REPO}")
    print(f"LOKI_PUSH_URL: {LOKI_PUSH_URL}")
    print(f"POLL_INTERVAL: {POLL_INTERVAL}s")
    
    start_http_server(PORT)
    print(f"Metrics server started on :{PORT}/metrics")
    
    t = threading.Thread(target=poll_loop, daemon=True)
    t.start()
    print("Poll loop thread started")
    
    # block main thread
    try:
        while True:
            time.sleep(60)
    except KeyboardInterrupt:
        print("Shutting down...")
        pass
