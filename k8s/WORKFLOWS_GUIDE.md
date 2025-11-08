# 📋 GitHub Actions Workflows - Tổng quan

## 🎯 Mục đích

Deploy ứng dụng CNPM-Food lên **Docker Desktop Kubernetes** sử dụng **GitHub Actions** với **self-hosted runner**.

---

## 📁 Workflows có sẵn

### 1. `deploy-k8s.yml` - Full CI/CD Pipeline ⭐
**Trigger:** 
- Tự động khi push lên branch `main` hoặc `src`
- Manual trigger (workflow_dispatch)

**Các bước:**
1. ✅ Login Docker Hub
2. ✅ Build tất cả Docker images (6 services)
3. ✅ Push images lên Docker Hub (`onlykohi` registry)
4. ✅ Deploy MySQL và đợi ready
5. ✅ Deploy tất cả microservices
6. ✅ Deploy monitoring stack (Prometheus + Grafana)
7. ✅ Verify deployment

**Thời gian:** 10-15 phút

**Use case:** 
- Deploy sau khi sửa code
- Tự động build images mới
- Full deployment từ đầu

---

### 2. `deploy-only.yml` - Deploy Only (Không build)
**Trigger:** Manual only (workflow_dispatch)

**Input options:**
- `deploy_monitoring`: Có deploy monitoring không? (true/false)

**Các bước:**
1. ✅ Verify Kubernetes context
2. ✅ Create namespaces
3. ✅ Deploy MySQL
4. ✅ Deploy microservices
5. ✅ Deploy monitoring (optional)
6. ✅ Show deployment summary

**Thời gian:** 3-5 phút

**Use case:**
- Chỉ deploy lại K8s manifests (không build images)
- Update config (env vars, resources, probes)
- Rollback về version cũ
- Nhanh hơn deploy-k8s.yml

---

## 🚀 Cách sử dụng

### Setup lần đầu (1 lần duy nhất):

1. **Setup self-hosted runner** - Xem `k8s/GITHUB_RUNNER_SETUP.md`
2. **Add Docker Hub secrets:**
   - `DOCKER_USERNAME`: onlykohi
   - `DOCKER_PASSWORD`: (Docker Hub access token)

### Sử dụng hàng ngày:

#### Option A: Tự động (Push code)
```powershell
git add .
git commit -m "Update code"
git push origin src
# → Workflow deploy-k8s.yml tự động chạy
```

#### Option B: Manual trigger
1. Vào GitHub: **Actions** tab
2. Chọn workflow muốn chạy:
   - **CI/CD Pipeline** (full build + deploy)
   - **Deploy Only** (chỉ deploy)
3. Click **Run workflow**
4. Chọn branch: `src`
5. (Deploy Only) Chọn có deploy monitoring không
6. Click **Run workflow**

---

## 📊 So sánh Workflows

| Feature | deploy-k8s.yml | deploy-only.yml |
|---------|----------------|-----------------|
| **Trigger** | Auto + Manual | Manual only |
| **Build images** | ✅ Yes | ❌ No |
| **Push Docker Hub** | ✅ Yes | ❌ No |
| **Deploy K8s** | ✅ Yes | ✅ Yes |
| **Monitoring** | ✅ Always | 🔧 Optional |
| **Time** | 10-15 min | 3-5 min |
| **Use when** | Code changed | Config changed |

---

## 🎓 Khi nào dùng workflow nào?

### Dùng `deploy-k8s.yml` khi:
- ✅ Sửa code Java/TypeScript
- ✅ Thay đổi Dockerfile
- ✅ Cần images mới
- ✅ Deploy lần đầu
- ✅ Sau khi merge PR

### Dùng `deploy-only.yml` khi:
- ✅ Chỉ sửa K8s YAML (resources, replicas, env vars)
- ✅ Update ConfigMap
- ✅ Thay đổi probe delays
- ✅ Test deployment nhanh
- ✅ Rollback (dùng image cũ)

---

## 🔍 Monitor Workflow

### Xem logs real-time:
1. **GitHub → Actions** tab
2. Click vào workflow run
3. Click vào job "build-and-deploy" hoặc "deploy"
4. Mở từng step để xem logs chi tiết

### Verify deployment trên máy:
```powershell
# Check pods
kubectl get pods -n cnpm-food

# Check services
kubectl get svc -n cnpm-food

# Check deployments
kubectl get deployments -n cnpm-food

# Logs của service
kubectl logs -f deployment/user-service -n cnpm-food
```

---

## ⚠️ Common Issues

### 1. Runner offline
**Triệu chứng:** Workflow stuck ở "Waiting for a runner..."

**Fix:**
```powershell
cd C:\actions-runner
./svc.sh status  # Check service
./svc.sh start   # Start nếu stopped
```

### 2. Docker login failed
**Triệu chứng:** Error: "unauthorized: incorrect username or password"

**Fix:**
- Kiểm tra `DOCKER_USERNAME` và `DOCKER_PASSWORD` secrets
- Tạo lại Docker Hub access token
- Update secrets trong GitHub

### 3. Kubectl command not found
**Triệu chứng:** "kubectl: command not found"

**Fix:**
```powershell
# Add kubectl to PATH
# Settings → System → Environment Variables
# Add: C:\Program Files\Docker\Docker\resources\bin
```

### 4. Pods stuck ở Pending
**Triệu chứng:** "Insufficient CPU" hoặc pods không start

**Fix:**
```powershell
# Docker Desktop → Settings → Resources
# Tăng CPU: 4 cores
# Tăng Memory: 8 GB
# Apply & Restart
```

### 5. ImagePullBackOff
**Triệu chứng:** Cannot pull image từ Docker Hub

**Fix:**
- Kiểm tra images đã push lên Docker Hub chưa
- Verify image name trong deployment YAML
- Check Docker Hub username đúng chưa

---

## 📝 Workflow Customization

### Thêm notification Slack:
```yaml
- name: Notify Slack
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### Thêm email notification:
```yaml
- name: Send email
  uses: dawidd6/action-send-mail@v3
  with:
    server_address: smtp.gmail.com
    server_port: 465
    username: ${{ secrets.MAIL_USERNAME }}
    password: ${{ secrets.MAIL_PASSWORD }}
    subject: Deployment Status
    body: Deployment completed!
```

### Thêm rollback step:
```yaml
- name: Rollback on failure
  if: failure()
  run: |
    kubectl rollout undo deployment/api-gateway -n cnpm-food
    kubectl rollout undo deployment/user-service -n cnpm-food
```

---

## 🎯 Best Practices

### Security:
- ✅ Dùng secrets cho credentials
- ✅ Không commit passwords vào code
- ✅ Rotate Docker Hub tokens định kỳ
- ✅ Giới hạn runner permissions

### Performance:
- ✅ Cache Docker layers
- ✅ Build parallel khi có thể
- ✅ Dùng `deploy-only.yml` khi không cần build

### Reliability:
- ✅ Add timeout cho mỗi step
- ✅ Use `continue-on-error` cho non-critical steps
- ✅ Verify deployment sau khi deploy
- ✅ Test workflow trên branch riêng trước

---

## ✅ Checklist trước khi chạy workflow

- [ ] Self-hosted runner đang online (Idle status)
- [ ] Docker Desktop đang chạy
- [ ] Kubernetes enabled trong Docker Desktop
- [ ] `kubectl get nodes` hoạt động
- [ ] Docker Hub secrets đã setup
- [ ] Code đã commit và push (nếu auto trigger)
- [ ] Có đủ resources (CPU: 4 cores, RAM: 8GB)

---

## 📚 Tài liệu liên quan

- **Setup Runner:** `k8s/GITHUB_RUNNER_SETUP.md`
- **Deploy Guide:** `k8s/QUICKSTART.md`
- **Monitoring:** `k8s/monitoring/README.md`
- **Architecture:** `k8s/ARCHITECTURE.md`

---

## 🆘 Support

**Issues?**
1. Check runner status trong GitHub
2. Xem workflow logs chi tiết
3. Verify kubectl hoạt động trên máy
4. Check Docker Desktop resources
5. Xem pods logs: `kubectl logs -f <pod-name> -n cnpm-food`

**Cần help?**
- GitHub Issues: https://github.com/SupremeJelly/CNPM-Food/issues
- Check existing workflows trong Actions tab
- Review logs từ successful runs

---

## 🎉 Kết luận

Với 2 workflows này, bạn có thể:
- ✅ Tự động deploy khi push code
- ✅ Manual deploy nhanh khi cần
- ✅ Build và deploy hoàn toàn automated
- ✅ Monitor deployment qua GitHub Actions UI
- ✅ Rollback nhanh khi cần

**Happy deploying!** 🚀
