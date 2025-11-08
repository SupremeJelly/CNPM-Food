# 💰 Cloud Cost Calculator - CNPM Food

## 📊 Resource Requirements

### **Current Configuration:**
- **Pods**: 7 (MySQL + 6 microservices)
- **CPU Total**: 1.85 cores (250m × 6 services + 100m frontend + 500m MySQL)
- **Memory Total**: 4GB (512Mi × 6 + 256Mi frontend + 1Gi MySQL)
- **Storage**: 10GB (MySQL PersistentVolume)
- **LoadBalancers**: 2 (Frontend + API Gateway)

---

## ☁️ Azure AKS Cost Breakdown

### **Cluster Nodes** (3 × Standard_B2s)
- vCPU: 2 cores each = 6 cores total
- RAM: 4GB each = 12GB total
- Cost: **$30.66** × 3 = **$92/month**

### **Managed Kubernetes Service**
- Control plane: **FREE** (Azure AKS không tính phí control plane!)

### **Load Balancer**
- Basic: **$18.40/month**
- 2 LBs needed: **$36.80/month**

### **Managed Disk** (Premium SSD)
- 10GB for MySQL: **$2.05/month**

### **Bandwidth** (Data Transfer Out)
- First 100GB free
- Additional: **$0.087/GB**
- Estimate 50GB/month: **$0**

### **Azure Monitor** (included with cluster)
- Basic metrics: **FREE**
- Full container insights: **~$10/month**

**Total Azure AKS: ~$140/month** (with monitoring)
**Without monitoring: ~$130/month**

---

## ☁️ Google GKE Cost Breakdown

### **Cluster Nodes** (3 × e2-medium)
- vCPU: 2 cores each = 6 cores total
- RAM: 4GB each = 12GB total
- Cost: **$24.27** × 3 = **$72.81/month**

### **GKE Management Fee**
- Control plane: **$73/month** (charged per cluster!)

### **Load Balancer**
- Network LB: **$18.40/month**
- 2 LBs: **$36.80/month**

### **Persistent Disk** (Standard)
- 10GB: **$0.40/month**

### **Egress Traffic**
- First 1GB free
- Next 10TB: **$0.12/GB**
- Estimate 50GB: **$6/month**

**Total GKE: ~$189/month**

---

## ☁️ AWS EKS Cost Breakdown

### **EKS Cluster**
- Control plane: **$73/month** (fixed per cluster)

### **EC2 Instances** (3 × t3.medium)
- vCPU: 2 cores each
- RAM: 4GB each
- Cost: **$30.37** × 3 = **$91.11/month**

### **Elastic Load Balancer** (Classic)
- Cost: **$22.68/month**
- 2 ELBs: **$45.36/month**

### **EBS Volumes** (General Purpose SSD)
- 10GB × 3 (mỗi node): **$1/month**
- MySQL volume 10GB: **$1/month**

### **Data Transfer**
- First 100GB free
- Additional: **$0.09/GB**

**Total AWS EKS: ~$211/month** (Đắt nhất!)

---

## ☁️ DigitalOcean DOKS Cost Breakdown

### **Cluster Nodes** (3 × 2vCPU 4GB)
- Cost: **$24/month** × 3 = **$72/month**

### **Kubernetes Control Plane**
- **FREE!** (DigitalOcean không tính phí)

### **Load Balancer**
- Cost: **$12/month** per LB
- 2 LBs: **$24/month**

### **Block Storage**
- 10GB: **$1/month**

### **Bandwidth**
- 1TB free per droplet
- Total 3TB free
- Additional: **$0.01/GB**

**Total DigitalOcean: ~$97/month**

---

## ☁️ Linode LKE Cost Breakdown

### **Cluster Nodes** (3 × Linode 4GB)
- vCPU: 2 cores each
- RAM: 4GB each
- Cost: **$24/month** × 3 = **$72/month**

### **Control Plane**: **FREE**

### **NodeBalancer** (Load Balancer)
- **$10/month** per LB
- 2 LBs: **$20/month**

### **Block Storage**
- 10GB: **$1/month**

**Total Linode: ~$93/month**

---

## ☁️ Civo Cloud Cost Breakdown

### **Cluster Nodes** (3 × Medium 2vCPU 4GB)
- Cost: **$20/month** × 3 = **$60/month**

### **Control Plane**: **FREE**

### **Load Balancer**: **FREE!** (included)

### **Storage**
- 10GB SSD: **$1/month**

**Total Civo: ~$61/month** (Rẻ nhất!)

---

## 📊 Cost Comparison Table

| Cloud Provider | Monthly Cost | Free Credit | Net Cost (First Month) | Difficulty |
|----------------|-------------|-------------|------------------------|------------|
| **Civo** | $61 | $250 | **$0** | ⭐ Easy |
| **DigitalOcean** | $97 | $200 | **$0** | ⭐ Easy |
| **Linode** | $93 | $100 | **$0** | ⭐ Easy |
| **Azure AKS** | $130 | $200 | **$0** | ⭐⭐ Medium |
| **Google GKE** | $189 | $300 | **$0** | ⭐⭐ Medium |
| **AWS EKS** | $211 | $0 | **$211** | ⭐⭐⭐ Hard |

---

## 💡 Cost Optimization Tips

### **1. Use Spot/Preemptible Instances**
- **Azure**: Spot VMs save ~70-90%
- **GCP**: Preemptible VMs save ~80%
- **AWS**: Spot Instances save ~70%

⚠️ Risk: Instances có thể bị terminate bất kỳ lúc nào

### **2. Scale Down Non-Peak Hours**
```bash
# Scale to 0 replicas khi không dùng (10PM - 8AM)
kubectl scale deployment --all --replicas=0 -n cnpm-food

# Scale lại khi cần
kubectl scale deployment --all --replicas=1 -n cnpm-food
```
**Tiết kiệm**: ~40% chi phí

### **3. Use Reserved Instances** (Production only)
- 1-year commitment: Save 30-40%
- 3-year commitment: Save 50-60%

### **4. Reduce Node Count**
Nếu chỉ demo/development:
```bash
# 2 nodes thay vì 3
# Cần scale down replicas về 1 cho mỗi service
```
**Tiết kiệm**: ~33% chi phí nodes

### **5. Use NodePort Instead of LoadBalancer**
```yaml
# Trong service definition
type: NodePort  # Thay vì LoadBalancer
```
**Tiết kiệm**: $20-40/month (tùy cloud)

⚠️ Trade-off: Cần thêm bước expose ports

### **6. Shared Cluster**
Deploy nhiều projects trên 1 cluster thay vì 1 cluster/project
**Tiết kiệm**: $73-146/month (control plane fees)

---

## 🎯 Recommended Setup by Budget

### **$0/month - Free Tier Only**
- **Cloud**: Civo ($250 credit) hoặc DigitalOcean ($200 credit)
- **Nodes**: 2 × Small (1vCPU 2GB)
- **Duration**: 2-4 tháng miễn phí
- **Setup**:
  ```bash
  # Reduce resource requests in deployments
  resources:
    requests:
      cpu: 100m
      memory: 256Mi
  ```

### **< $50/month - Student Budget**
- **Cloud**: Civo
- **Nodes**: 2 × Medium (2vCPU 4GB)
- **Scale**: 1 replica per service
- **Cost**: ~$40/month

### **$50-100/month - Small Production**
- **Cloud**: DigitalOcean hoặc Linode
- **Nodes**: 3 × Medium (2vCPU 4GB)
- **Scale**: 2 replicas for critical services
- **Cost**: $90-100/month

### **$100-200/month - Production**
- **Cloud**: Azure AKS
- **Nodes**: 3 × Standard_B2s + Auto-scaling
- **Features**: Full monitoring, backup
- **Cost**: $130-180/month

---

## 📅 Cost Over Time (with Free Credits)

### **Civo Cloud**
- Month 1: $0 (using $250 credit)
- Month 2-4: $0 (credit remaining)
- Month 5+: $61/month

### **DigitalOcean**
- Month 1-2: $0 (using $200 credit)
- Month 3+: $97/month

### **Azure AKS**
- Month 1: $0 (using $200 credit)
- Month 2+: $130/month

### **Google GKE**
- Month 1-3: $0 (using $300 credit)
- Month 4+: $189/month

---

## 🔍 Hidden Costs to Watch

1. **Data Transfer Out** - Nếu app có nhiều traffic
2. **Snapshots/Backups** - $0.05/GB/month
3. **Log Storage** - Nếu enable full logging
4. **SSL Certificates** - Free với Let's Encrypt
5. **Domain Name** - $10-15/year
6. **Monitoring Services** - $10-50/month

---

## 💰 Total Cost of Ownership (1 năm)

| Provider | Setup | Monthly | Annual Total |
|----------|-------|---------|--------------|
| Civo | Free | $61 | **$488** (sau credit) |
| DigitalOcean | Free | $97 | **$970** (sau credit) |
| Azure | Free | $130 | **$1,430** (sau credit) |
| GKE | Free | $189 | **$1,701** (sau credit) |
| AWS | Free | $211 | **$2,532** |

---

## ✅ Final Recommendation

### **Học tập / Demo (< 3 tháng):**
👉 **Civo Cloud**
- Free credit cao nhất ($250)
- Rẻ nhất ($61/month)
- Setup nhanh nhất

### **Production nhỏ (Startup):**
👉 **DigitalOcean**
- Giá tốt ($97/month)
- Docs tốt nhất
- Support tốt

### **Production lớn (Enterprise):**
👉 **Azure AKS**
- Tích hợp ecosystem
- Security tốt
- SLA 99.95%

---

**Lưu ý**: Giá cả có thể thay đổi. Kiểm tra pricing calculator của từng cloud provider để có số liệu chính xác nhất.
