import os
import time
import threading
import requests
from prometheus_client import start_http_server, Gauge, Counter

G_UP = Gauge('github_exporter_up', 'GitHub API up (1 = ok, 0 = failed)')
G_RATE_REMAIN = Gauge('github_rate_limit_remaining', 'GitHub rate limit remaining')
C_CHECKS = Counter('github_exporter_checks_total', 'Number of GitHub checks performed')

GITHUB_TOKEN = os.environ.get('GITHUB_TOKEN')
POLL_INTERVAL = int(os.environ.get('POLL_INTERVAL', '30'))
PORT = int(os.environ.get('METRICS_PORT', '9118'))

HEADERS = {'Accept': 'application/vnd.github.v3+json'}
if GITHUB_TOKEN:
    HEADERS['Authorization'] = f'token {GITHUB_TOKEN}'


def poll_loop():
    while True:
        try:
            r = requests.get('https://api.github.com/rate_limit', headers=HEADERS, timeout=10)
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
        except Exception:
            G_UP.set(0)
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
