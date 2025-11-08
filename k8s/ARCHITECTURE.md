# 🏗️ Kubernetes Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          KUBERNETES CLUSTER                              │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │                    Namespace: cnpm-food                         │    │
│  │                                                                 │    │
│  │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │    │
│  │  │   Frontend   │    │ API Gateway  │    │ User Service │     │    │
│  │  │ (2 replicas) │    │ (2 replicas) │    │ (2 replicas) │     │    │
│  │  │   Port 4200  │◄───│  Port 9000   │◄───│  Port 8081   │     │    │
│  │  └──────────────┘    └──────────────┘    └──────────────┘     │    │
│  │         │                    │                    │            │    │
│  │         │                    │                    ▼            │    │
│  │         │                    │            ┌──────────────┐     │    │
│  │         │                    │            │ Restaurant   │     │    │
│  │         │                    └───────────►│   Service    │     │    │
│  │         │                                 │ (2 replicas) │     │    │
│  │         │                                 │  Port 8082   │     │    │
│  │         │                                 └──────────────┘     │    │
│  │         │                                        │             │    │
│  │         │                    ┌──────────────┐   │             │    │
│  │         │                    │ Order Service│◄──┘             │    │
│  │         │                    │ (2 replicas) │                 │    │
│  │         │                    │  Port 8083   │                 │    │
│  │         │                    └──────────────┘                 │    │
│  │         │                           │                         │    │
│  │         │                           ▼                         │    │
│  │         │                    ┌──────────────┐                 │    │
│  │         │                    │Payment Svc   │                 │    │
│  │         │                    │ (2 replicas) │                 │    │
│  │         │                    │  Port 8085   │                 │    │
│  │         │                    └──────────────┘                 │    │
│  │         │                           │                         │    │
│  │         │                           ▼                         │    │
│  │         │                    ┌──────────────┐                 │    │
│  │         └───────────────────►│    MySQL     │◄────────────────┤    │
│  │                              │    (1 pod)   │                 │    │
│  │                              │  Port 3306   │                 │    │
│  │                              │   + PVC 10GB │                 │    │
│  │                              └──────────────┘                 │    │
│  │                                                                │    │
│  └────────────────────────────────────────────────────────────────┘    │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │                  Namespace: monitoring                          │    │
│  │                                                                 │    │
│  │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │    │
│  │  │  Prometheus  │◄───│ ServiceMonitor│───►│   Grafana    │     │    │
│  │  │  (scrapes    │    │ (auto-discover│    │ (dashboards) │     │    │
│  │  │   metrics)   │    │   services)   │    │              │     │    │
│  │  │  Port 9090   │    └──────────────┘    │  Port 3000   │     │    │
│  │  └──────────────┘                         └──────────────┘     │    │
│  │         │                                         ▲             │    │
│  │         │                                         │             │    │
│  │         └─────────── Scrapes every 15s ──────────┘             │    │
│  │                                                                 │    │
│  └────────────────────────────────────────────────────────────────┘    │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │                     Ingress Controller                          │    │
│  │                                                                 │    │
│  │         cnpm-food.example.com ───► Frontend (Port 80)          │    │
│  │         cnpm-food.example.com/api ───► API Gateway (Port 9000) │    │
│  │         grafana.cnpm-food.example.com ───► Grafana (Port 3000) │    │
│  │                                                                 │    │
│  └────────────────────────────────────────────────────────────────┘    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
                    ┌─────────────────────────┐
                    │   Load Balancer (Azure) │
                    │   External IP: x.x.x.x  │
                    └─────────────────────────┘
                                  │
                                  ▼
                         ┌────────────────┐
                         │   Internet     │
                         │   Users        │
                         └────────────────┘
```

---

## 📊 Component Breakdown

### Application Layer (cnpm-food namespace)
- **Frontend**: Angular app, serves UI
- **API Gateway**: Routes requests to microservices
- **User Service**: User authentication & management
- **Restaurant Service**: Restaurant & menu management
- **Order Service**: Order processing
- **Payment Service**: Payment handling
- **MySQL**: Persistent storage (4 databases)

### Monitoring Layer (monitoring namespace)
- **Prometheus**: Metrics collection & storage
- **Grafana**: Visualization & dashboards
- **ServiceMonitor**: Auto-discovery of services
- **Alertmanager**: Alert notifications (optional)

### Network Layer
- **Ingress Controller**: External traffic routing
- **Load Balancer**: Azure/Cloud load balancer
- **ClusterIP Services**: Internal communication
- **LoadBalancer Services**: External access

---

## 🔄 Data Flow

### User Request Flow:
```
Internet User
    │
    ▼
Load Balancer (Azure)
    │
    ▼
Ingress Controller
    │
    ▼
Frontend Service (ClusterIP)
    │
    ▼
Frontend Pod
    │
    ▼
API Gateway Service (ClusterIP)
    │
    ▼
API Gateway Pod
    │
    ├──► User Service ──► MySQL (user_db)
    ├──► Restaurant Service ──► MySQL (restaurant_db)
    ├──► Order Service ──► MySQL (order_db)
    └──► Payment Service ──► MySQL (payment_db)
```

### Monitoring Flow:
```
Microservices (expose /actuator/prometheus)
    │
    ▼
ServiceMonitor (CRD)
    │
    ▼
Prometheus (scrapes every 15s)
    │
    ▼
Prometheus Storage (TSDB)
    │
    ▼
Grafana (queries Prometheus)
    │
    ▼
Dashboard Visualization
```

---

## 🎯 Scalability

### Horizontal Scaling (HPA):
```
Low Load (2 replicas):
Frontend:    2 pods
API Gateway: 2 pods
User Svc:    2 pods
Restaurant:  2 pods
Order Svc:   2 pods
Payment Svc: 2 pods
-----------------------
Total: 12 pods

High Load (10 replicas):
Frontend:    2 pods (static content)
API Gateway: 10 pods
User Svc:    10 pods
Restaurant:  10 pods
Order Svc:   10 pods
Payment Svc: 10 pods
-----------------------
Total: 52 pods
```

### Vertical Scaling (Resource Limits):
```
Each Pod:
  Requests: 250m CPU, 512Mi Memory
  Limits:   500m CPU, 1Gi Memory

Max Node Capacity (Standard_B2s):
  2 vCPU, 4GB RAM
  Can run ~6-8 pods per node
```

---

## 🔐 Security Layers

```
┌─────────────────────────────────────┐
│   1. Network Policy                 │ ← Pod-to-pod firewall
├─────────────────────────────────────┤
│   2. RBAC (Role-Based Access)       │ ← User permissions
├─────────────────────────────────────┤
│   3. Secrets (encrypted at rest)    │ ← Sensitive data
├─────────────────────────────────────┤
│   4. Service Mesh (Istio - optional)│ ← mTLS encryption
├─────────────────────────────────────┤
│   5. Image Scanning (Trivy)         │ ← Vulnerability detection
└─────────────────────────────────────┘
```

---

## 💾 Storage

### Persistent Volumes:
```
MySQL PVC:
  Size: 10Gi
  StorageClass: managed-csi (Azure Disk)
  Access Mode: ReadWriteOnce
  
Prometheus PVC (auto-created by Helm):
  Size: 8Gi
  StorageClass: managed-csi
  
Grafana PVC (auto-created by Helm):
  Size: 2Gi
  StorageClass: managed-csi
```

---

## 📈 Resource Requirements

### Minimum Cluster:
```
Nodes: 2 x Standard_B2s (2 vCPU, 4GB RAM each)
Total: 4 vCPU, 8GB RAM
Cost: ~$60/month
```

### Recommended Cluster:
```
Nodes: 3 x Standard_B2ms (2 vCPU, 8GB RAM each)
Total: 6 vCPU, 24GB RAM
Cost: ~$120/month
```

### Production Cluster:
```
Nodes: 5 x Standard_D2s_v3 (2 vCPU, 8GB RAM each)
Total: 10 vCPU, 40GB RAM
Cost: ~$300/month
```

---

**Last Updated**: 2025-11-07
