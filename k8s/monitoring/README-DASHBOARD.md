# GitHub Monitoring Dashboard Guide

## 📊 Dashboard Overview

Dashboard hiển thị real-time monitoring cho GitHub Actions workflows với focus vào CI/CD failures và test results.

**Access:** http://localhost:30300 → Dashboards → "GitHub Exporter - CNPM Food"

---

## 🎯 Dashboard Layout

### Row 1: Summary Cards (Top)
| Card | Metric | Color Logic |
|------|--------|-------------|
| 🚨 **Failing Workflows** | Số workflows đang fail | Green (0), Red (≥1) |
| ❌ **Failing Jobs** | Tổng jobs fail | Green (0), Orange (1-2), Red (≥3) |
| 🧪 **Failed Tests** | Tổng test cases fail | Green (0), Red (≥1) |
| ✅ **Exporter** | Status của exporter | Green (UP), Red (DOWN) |
| 🎯 **Rate Limit** | GitHub API calls còn lại | Red (<1000), Orange (1000-3000), Green (>3000) |

### Row 2: Failing Jobs Details
- **🔴 Failing Jobs Table**: Hiển thị workflow + job đang fail
  - Columns: Workflow, Job
  - Auto-refresh mỗi 30s
  
- **⏰ Last Failed Table**: Timestamp lần fail gần nhất
  - Columns: Workflow Name, Last Failed (ISO format)
  - Sort theo thời gian mới nhất

### Row 3: Test Results
- **🧪 Failed Test Cases**: Top 15 test cases fail nhiều nhất
  - Columns: Test (test name), Count (số lần fail)
  - Footer: Tổng số failures
  - Background đỏ để dễ nhận diện

### Row 4: Detailed Logs
- **📋 Failure Details**: Logs từ Loki
  - Hiển thị: Steps fail, Test failures, Test summaries
  - Deduplication: Exact (loại bỏ duplicate)
  - Labels: workflow, job, run_id (để filter)

---

## 📝 Structured Test Logging Format

Để dashboard parse chính xác, workflows nên in logs theo format:

```yaml
- name: Run Tests
  run: |
    echo "===== TEST EXECUTION START ====="
    echo "TEST_SUITE: Integration Tests"
    echo "TEST_TOTAL: 5"
    echo ""
    
    echo "TEST_RUN: test_user_authentication"
    echo "TEST_STATUS: PASS"
    echo ""
    
    echo "TEST_RUN: test_api_validation"
    echo "TEST_CLASS: com.example.ApiTest"  # Optional
    echo "TEST_STATUS: FAIL"
    echo "TEST_ERROR: Expected 200 but got 500"  # Optional
    echo ""
    
    echo "===== TEST SUMMARY ====="
    echo "TEST_PASSED: 3"
    echo "TEST_FAILED: 2"
    exit 1
```

### Key Fields:
- `TEST_RUN`: Tên test case
- `TEST_CLASS`: Class chứa test (optional, for Java/Maven)
- `TEST_STATUS`: PASS | FAIL | ERROR
- `TEST_ERROR`: Error message (optional)
- `TEST_TOTAL`, `TEST_PASSED`, `TEST_FAILED`: Summary stats

---

## 🔍 How to Use

### 1. Monitor Active Failures
- Check **Summary Cards** để biết overview
- Nếu có failures → xem **Failing Jobs table** để biết workflow + job nào fail

### 2. Investigate Test Failures
- Check **Failed Test Cases** table để xem test nào fail nhiều nhất
- Click vào **Failure Details logs** để xem:
  - Steps nào trong job fail
  - Test cases cụ thể nào fail
  - Error messages

### 3. Track Over Time
- **Last Failed** table cho biết workflow nào fail gần đây
- Use time picker (top right) để xem historical data

### 4. Filter Logs
- Trong **Failure Details** panel:
  - Click "Show labels" để thấy workflow/job/run_id
  - Use label filters: `{workflow="Test Monitoring"}`
  - Search text: type keywords vào search box

---

## 🚀 Metrics Explained

| Metric | Description | Query |
|--------|-------------|-------|
| `github_workflow_failed_job` | Gauge (0/1) cho mỗi job | `== 1` để filter failures |
| `github_workflow_last_failed_timestamp_seconds` | Unix timestamp lần fail cuối | `* 1000` convert to ms |
| `github_workflow_testcase_failed_total` | Counter số lần test fail | `> 0` để filter |
| `github_workflow_step_failed_total` | Counter steps fail | Grouped by workflow/job/step |

---

## 🔧 Troubleshooting

### Dashboard shows "No data"
1. Check **Exporter card** = UP (green)
2. Verify Prometheus scraping: http://localhost:30090/targets
3. Check exporter logs: `kubectl logs -l app=github-exporter -n monitoring`

### Test failures not showing
1. Verify workflow uses structured logging format (see above)
2. Check workflow actually failed (not just skipped)
3. Wait 30s for next poll cycle

### Logs are empty
1. Check Loki is running: `kubectl get pods -n monitoring | grep loki`
2. Verify Loki URL in exporter: Should be `http://loki-stack.monitoring.svc.cluster.local:3100`
3. Check exporter pushed logs: Look for `LOKI_PUSHES` metric

---

## 📌 Tips

1. **Use Test Workflow** (`.github/workflows/test-monitoring.yml`) để test monitoring setup
2. **Refresh Rate**: Dashboard auto-refresh every 30s, metrics polled every 30s
3. **Time Range**: Default last 1h, có thể adjust ở top-right corner
4. **Labels**: Hover over table cells để see full text nếu bị truncate

---

## 🛠 Customization

Edit dashboard: `k8s/monitoring/grafana-dashboard-github.yaml`

After changes:
```bash
kubectl apply -f k8s/monitoring/grafana-dashboard-github.yaml
kubectl rollout restart deployment grafana -n monitoring
```

---

**Last Updated**: 2025-11-23
