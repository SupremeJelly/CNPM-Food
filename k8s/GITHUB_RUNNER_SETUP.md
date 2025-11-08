# 🚀 Setup GitHub Actions Self-Hosted Runner cho Docker Desktop

## Tổng quan
Để GitHub Actions có thể deploy lên Kubernetes của Docker Desktop (chạy trên máy local), bạn cần setup một **self-hosted runner** trên máy Windows.

---

## 📋 Bước 1: Chuẩn bị

### Yêu cầu:
- ✅ Docker Desktop đã cài và Kubernetes enabled
- ✅ Git đã cài
- ✅ PowerShell (Windows)
- ✅ Có quyền Admin trên máy

### Kiểm tra Kubernetes:
```powershell
kubectl config current-context
# Nên thấy: docker-desktop

kubectl get nodes
# Nên thấy: docker-desktop   Ready
```

---

## 📋 Bước 2: Tạo Self-Hosted Runner trên GitHub

### 2.1. Vào Repository Settings:
1. Mở GitHub repository: https://github.com/SupremeJelly/CNPM-Food
2. Click **Settings** (tab trên cùng)
3. Sidebar trái → Click **Actions** → **Runners**
4. Click nút **New self-hosted runner**

### 2.2. Chọn hệ điều hành:
- Operating System: **Windows**
- Architecture: **x64**

### 2.3. Copy commands và chạy trên máy:

GitHub sẽ hiển thị các commands. Mở **PowerShell as Administrator** và chạy:

```powershell
# 1. Tạo thư mục cho runner
mkdir C:\actions-runner ; cd C:\actions-runner

# 2. Download runner package (copy command từ GitHub)
Invoke-WebRequest -Uri https://github.com/actions/runner/releases/download/v2.311.0/actions-runner-win-x64-2.311.0.zip -OutFile actions-runner-win-x64-2.311.0.zip

# 3. Extract
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory("$PWD/actions-runner-win-x64-2.311.0.zip", "$PWD")

# 4. Configure runner (copy command từ GitHub - có token riêng)
./config.cmd --url https://github.com/SupremeJelly/CNPM-Food --token YOUR_TOKEN_HERE

# Trả lời các câu hỏi:
# - Enter the name of the runner group: [Press Enter for default]
# - Enter the name of runner: [Nhập: docker-desktop-runner]
# - Enter any additional labels: [Nhập: docker-desktop,local]
# - Enter name of work folder: [Press Enter for default: _work]
```

---

## 📋 Bước 3: Chạy Runner

### Option A: Chạy Interactive (Test)
```powershell
cd C:\actions-runner
./run.cmd
```

**Giữ cửa sổ PowerShell mở!** Runner sẽ ngừng khi đóng cửa sổ.

### Option B: Chạy như Windows Service (Recommended - Production)
```powershell
cd C:\actions-runner

# Install service (cần Admin)
./svc.sh install

# Start service
./svc.sh start

# Check status
./svc.sh status

# Stop service (khi cần)
./svc.sh stop
```

---

## 📋 Bước 4: Verify Runner đã Online

1. Vào GitHub: **Settings → Actions → Runners**
2. Xem runner với status **Idle** (màu xanh) ✅
3. Nếu offline (màu xám), kiểm tra lại runner đang chạy

---

## 📋 Bước 5: Thêm Docker Hub Secrets

GitHub Actions cần credentials để push images lên Docker Hub:

1. Vào **Settings → Secrets and variables → Actions**
2. Click **New repository secret**
3. Tạo 2 secrets:

**Secret 1:**
- Name: `DOCKER_USERNAME`
- Value: `onlykohi` (username Docker Hub của bạn)

**Secret 2:**
- Name: `DOCKER_PASSWORD`
- Value: (Docker Hub access token hoặc password)

### Cách tạo Docker Hub Access Token (Recommended):
1. Login Docker Hub: https://hub.docker.com
2. **Account Settings → Security → Access Tokens**
3. Click **New Access Token**
4. Name: `github-actions`
5. Permissions: **Read, Write, Delete**
6. Copy token và paste vào GitHub Secret

---

## 📋 Bước 6: Test Workflow

### 6.1. Commit và Push code:
```powershell
cd D:\CNPM-Food
git add .
git commit -m "Setup self-hosted runner for Docker Desktop K8s"
git push origin src
```

### 6.2. Xem workflow chạy:
1. Vào GitHub: **Actions** tab
2. Xem workflow "CI/CD Pipeline - Docker Desktop K8s" đang chạy
3. Click vào workflow để xem logs real-time

### 6.3. Verify deployment:
Sau khi workflow hoàn thành (5-10 phút):

```powershell
# Check pods
kubectl get pods -n cnpm-food

# Check deployments
kubectl get deployments -n cnpm-food

# Access frontend
kubectl port-forward svc/frontend -n cnpm-food 4200:80
# Mở browser: http://localhost:4200
```

---

## 🔧 Troubleshooting

### Runner không connect được GitHub?
```powershell
# 1. Check firewall
# Cho phép outbound connections đến github.com

# 2. Check proxy (nếu có)
# Set proxy trong runner config

# 3. Restart runner
cd C:\actions-runner
./svc.sh stop
./svc.sh start
```

### Workflow fail với "Permission denied"?
```powershell
# Runner cần quyền truy cập Docker và kubectl
# 1. Add user chạy runner vào docker-users group
net localgroup docker-users "YOUR_USERNAME" /add

# 2. Restart Docker Desktop

# 3. Restart runner service
```

### kubectl command not found?
```powershell
# 1. Kiểm tra kubectl có trong PATH không
kubectl version

# 2. Nếu không, add vào PATH
# Settings → System → Environment Variables
# Thêm: C:\Program Files\Docker\Docker\resources\bin
```

### Pods stuck ở Pending?
```powershell
# Docker Desktop resource limits
# 1. Open Docker Desktop
# 2. Settings → Resources
# 3. Tăng CPU: 4 cores, Memory: 8GB
# 4. Apply & Restart
```

---

## 📊 Workflow Features

Workflow hiện tại sẽ:

1. ✅ **Build** tất cả Docker images
2. ✅ **Push** images lên Docker Hub
3. ✅ **Deploy MySQL** và đợi ready
4. ✅ **Deploy** tất cả microservices
5. ✅ **Deploy monitoring** (Prometheus + Grafana)
6. ✅ **Verify** deployment status
7. ✅ **Show** pods và services

### Trigger workflow:
- **Tự động**: Khi push code lên branch `main` hoặc `src`
- **Manual**: Vào Actions → Chọn workflow → Click "Run workflow"

---

## 🎯 Manual Trigger (Không cần push code)

1. Vào GitHub: **Actions** tab
2. Chọn workflow "CI/CD Pipeline - Docker Desktop K8s"
3. Click **Run workflow** button (bên phải)
4. Chọn branch: `src`
5. Click **Run workflow** (xanh)

---

## 🗑️ Uninstall Runner (Khi không cần nữa)

```powershell
cd C:\actions-runner

# Stop và remove service
./svc.sh stop
./svc.sh uninstall

# Remove runner từ GitHub
./config.cmd remove --token YOUR_REMOVAL_TOKEN

# Xóa folder
cd ..
Remove-Item -Recurse -Force C:\actions-runner
```

---

## 📝 Best Practices

### Security:
- ✅ Không commit Docker Hub password vào code
- ✅ Dùng Access Token thay vì password
- ✅ Định kỳ rotate tokens (3-6 tháng)

### Runner Management:
- ✅ Chạy runner như Windows Service
- ✅ Set service Start Type = Automatic
- ✅ Monitor runner status trong GitHub

### Deployment:
- ✅ Test trên local trước khi push
- ✅ Xem logs trong GitHub Actions
- ✅ Verify pods sau mỗi deployment

---

## ✅ Checklist

- [ ] Docker Desktop Kubernetes enabled
- [ ] kubectl hoạt động (`kubectl get nodes`)
- [ ] Self-hosted runner đã setup
- [ ] Runner status = Idle (online)
- [ ] Docker Hub secrets đã tạo (DOCKER_USERNAME, DOCKER_PASSWORD)
- [ ] Workflow file đã commit và push
- [ ] Test workflow thành công
- [ ] Pods đang running (`kubectl get pods -n cnpm-food`)

---

## 🎉 Kết luận

Sau khi setup xong, mỗi lần bạn push code:

1. **GitHub Actions tự động chạy**
2. **Build images mới**
3. **Push lên Docker Hub**
4. **Deploy lên Docker Desktop K8s**
5. **Verify deployment**

**Tất cả tự động hoàn toàn!** 🚀

Frontend sẽ available tại: http://localhost (sau khi port-forward)
