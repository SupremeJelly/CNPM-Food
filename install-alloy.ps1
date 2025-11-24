# ==============================================================================
# CẤU HÌNH THÔNG TIN GRAFANA CLOUD CỦA BẠN TẠI ĐÂY
# ==============================================================================
# 1. User ID (Ví dụ: 1399942)
$LOKI_USER = "1399942" 

# 2. Token/Password (Access Policy Token có quyền logs:write)
$LOKI_PASS = "glc_eyJvIjoiMTU5MzAwMCIsIm4iOiJzdGFjay0xNDQyMTQ4LWhsLXdyaXRlLWxvZy13aXJ0ZSIsImsiOiJDcDYzUWE5MzY4NHEwY1ZYN2RHaGZHNHYiLCJtIjp7InIiOiJwcm9kLWFwLXNvdXRoZWFzdC0xIn19" 

# 3. URL Loki (Phải có đuôi /loki/api/v1/push)
$LOKI_URL = "https://logs-prod-020.grafana.net/loki/api/v1/push"

# ==============================================================================
# KHÔNG CẦN SỬA GÌ Ở DƯỚI ĐÂY - SCRIPT SẼ TỰ CHẠY
# ==============================================================================

Write-Host "dang khoi tao Grafana Alloy tren K8s..." -ForegroundColor Cyan

# 1. Tạo Namespace
Write-Host "1. Tao Namespace 'grafana-alloy'..."
kubectl create namespace grafana-alloy --dry-run=client -o yaml | kubectl apply -f -

# 2. Tạo Secret chứa mật khẩu
Write-Host "2. Tao Secret..."
kubectl create secret generic alloy-secrets -n grafana-alloy --dry-run=client -o yaml `
  --from-literal=LOKI_USER=$LOKI_USER `
  --from-literal=LOKI_PASS=$LOKI_PASS `
  --from-literal=LOKI_URL=$LOKI_URL | kubectl apply -f -

# 3. Tạo ConfigMap (File cấu hình Alloy)
Write-Host "3. Tao ConfigMap..."
$ConfigMapYaml = @"
apiVersion: v1
kind: ConfigMap
metadata:
  name: alloy-config
  namespace: grafana-alloy
data:
  config.alloy: |
    logging {
      level = "info"
      format = "logfmt"
    }

    // Tự động tìm Pods
    discovery.kubernetes "k8s_pods" {
      role = "pod"
    }

    // Đọc log từ Pods
    loki.source.kubernetes "pod_logs" {
      targets    = discovery.kubernetes.k8s_pods.targets
      forward_to = [loki.process.add_metadata.receiver]
    }

    // Thêm nhãn cluster
    loki.process "add_metadata" {
      stage.static_labels {
          values = {
              cluster = "k8s-cluster", 
          }
      }
      forward_to = [loki.write.grafana_cloud.receiver]
    }

    // Gửi lên Grafana Cloud
    loki.write "grafana_cloud" {
      endpoint {
        url = sys.env("LOKI_URL")
        basic_auth {
          username = sys.env("LOKI_USER")
          password = sys.env("LOKI_PASS")
        }
      }
    }
"@
$ConfigMapYaml | kubectl apply -f -

# 4. Tạo RBAC (Quyền truy cập) và DaemonSet (App chạy)
Write-Host "4. Deploy Alloy DaemonSet..."
$DeployYaml = @"
apiVersion: v1
kind: ServiceAccount
metadata:
  name: grafana-alloy
  namespace: grafana-alloy
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: grafana-alloy
rules:
- apiGroups: [""]
  resources: ["nodes", "nodes/proxy", "services", "endpoints", "pods"]
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: grafana-alloy
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: grafana-alloy
subjects:
- kind: ServiceAccount
  name: grafana-alloy
  namespace: grafana-alloy
---
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: grafana-alloy
  namespace: grafana-alloy
  labels:
    app: grafana-alloy
spec:
  selector:
    matchLabels:
      app: grafana-alloy
  template:
    metadata:
      labels:
        app: grafana-alloy
    spec:
      serviceAccountName: grafana-alloy
      containers:
        - name: grafana-alloy
          image: grafana/alloy:latest
          args:
            - run
            - --server.http.listen-addr=0.0.0.0:12345
            - /etc/alloy/config.alloy
          env:
            - name: LOKI_USER
              valueFrom: { secretKeyRef: { name: alloy-secrets, key: LOKI_USER } }
            - name: LOKI_PASS
              valueFrom: { secretKeyRef: { name: alloy-secrets, key: LOKI_PASS } }
            - name: LOKI_URL
              valueFrom: { secretKeyRef: { name: alloy-secrets, key: LOKI_URL } }
            - name: HOSTNAME
              valueFrom: { fieldRef: { fieldPath: spec.nodeName } }
          volumeMounts:
            - name: config
              mountPath: /etc/alloy
            - name: varlog
              mountPath: /var/log
              readOnly: true
            - name: varlibdocker
              mountPath: /var/lib/docker/containers
              readOnly: true
      volumes:
        - name: config
          configMap:
            name: alloy-config
        - name: varlog
          hostPath:
            path: /var/log
        - name: varlibdocker
          hostPath:
            path: /var/lib/docker/containers
"@
$DeployYaml | kubectl apply -f -

Write-Host "---"
Write-Host "✅ CAI DAT HOAN TAT!" -ForegroundColor Green
Write-Host "Kiem tra trang thai bang lenh: kubectl get pods -n grafana-alloy"