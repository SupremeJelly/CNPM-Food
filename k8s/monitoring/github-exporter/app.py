import os
import time
import threading
import requests
from prometheus_client import start_http_server, Gauge, Counter, Histogram

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

    while True:
        try:
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
                                                # Inline: push a short summary to Loki if configured
                                                try:
                                                    if LOKI_PUSH_URL:
                                                        # build a concise message
                                                        run_url = f"https://github.com/{owner_repo}/actions/runs/{run_id}"
                                                        step = job.get('name')
                                                        msg = f"FAILED: {wf_name} / {job_name} — url: {run_url}"
                                                        # prepare Loki push payload
                                                        ts_ns = str(int(time.time() * 1e9))
                                                        stream = {
                                                            "stream": {
                                                                "app": "github-exporter",
                                                                "repo": owner_repo,
                                                                "workflow": wf_name,
                                                                "run_id": str(run_id),
                                                                "job": job_name
                                                            },
                                                            "values": [[ts_ns, msg]]
                                                        }
                                                        payload = {"streams": [stream]}
                                                        headers_loki = {"Content-Type": "application/json"}
                                                        if LOKI_AUTH_HEADER:
                                                            headers_loki["Authorization"] = LOKI_AUTH_HEADER
                                                        try:
                                                            requests.post(LOKI_PUSH_URL, json=payload, headers=headers_loki, timeout=5)
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
                except Exception:
                    # don't let workflow polling break the main loop
                    pass
        except Exception:
            G_UP.set(0)
            REQ_ERRORS.inc()
        time.sleep(POLL_INTERVAL)


if __name__ == '__main__':
    start_http_server(PORT)
    t = threading.Thread(target=poll_loop, daemon=True)
    t.start()
    # block main thread
    try:
        while True:
            time.sleep(60)
    except KeyboardInterrupt:
        pass
