#!/bin/bash
# CNPM Food - Kubernetes Deployment Script
# This script automates the deployment of all services to Kubernetes

set -e  # Exit on error

echo "🚀 CNPM Food - Kubernetes Deployment"
echo "===================================="

# Configuration
NAMESPACE="cnpm-food"
DOCKER_REGISTRY="cnpmfoodacr.azurecr.io"  # Change to your registry
SERVICES=("user-service" "restaurant-service" "order-service" "payment-service" "api-gateway" "frontend")

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_step() {
    echo -e "${GREEN}[STEP]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Step 1: Check prerequisites
print_step "Checking prerequisites..."
command -v kubectl >/dev/null 2>&1 || { print_error "kubectl is not installed. Aborting."; exit 1; }
command -v docker >/dev/null 2>&1 || { print_error "docker is not installed. Aborting."; exit 1; }

# Step 2: Build Docker images
print_step "Building Docker images..."
for service in "${SERVICES[@]}"; do
    print_step "Building $service..."
    docker build -t $DOCKER_REGISTRY/$service:latest ./$service
done

# Step 3: Push images to registry
print_step "Pushing images to registry..."
read -p "Do you want to push images to registry? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    for service in "${SERVICES[@]}"; do
        print_step "Pushing $service..."
        docker push $DOCKER_REGISTRY/$service:latest
    done
fi

# Step 4: Create namespace
print_step "Creating namespace $NAMESPACE..."
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Step 5: Deploy MySQL
print_step "Deploying MySQL..."
kubectl apply -f k8s/services/mysql-deployment.yaml

print_step "Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n $NAMESPACE --timeout=300s || {
    print_error "MySQL failed to start"
    kubectl logs -l app=mysql -n $NAMESPACE --tail=50
    exit 1
}

# Step 6: Deploy microservices
print_step "Deploying microservices..."
kubectl apply -f k8s/services/user-service-deployment.yaml
kubectl apply -f k8s/services/restaurant-service-deployment.yaml
kubectl apply -f k8s/services/order-service-deployment.yaml
kubectl apply -f k8s/services/payment-service-deployment.yaml
kubectl apply -f k8s/services/api-gateway-deployment.yaml
kubectl apply -f k8s/services/frontend-deployment.yaml

# Step 7: Wait for services to be ready
print_step "Waiting for services to be ready..."
for service in "${SERVICES[@]}"; do
    print_step "Waiting for $service..."
    kubectl wait --for=condition=ready pod -l app=$service -n $NAMESPACE --timeout=300s || {
        print_warning "$service failed to start, checking logs..."
        kubectl logs -l app=$service -n $NAMESPACE --tail=50
    }
done

# Step 8: Install monitoring
print_step "Installing monitoring stack..."
read -p "Do you want to install Prometheus + Grafana monitoring? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
    helm repo update
    
    helm install monitoring prometheus-community/kube-prometheus-stack \
        --namespace monitoring \
        --create-namespace \
        --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
        --set grafana.adminPassword=admin \
        --wait
    
    # Apply ServiceMonitors
    kubectl apply -f k8s/monitoring/servicemonitor.yaml
    
    print_step "Monitoring installed successfully!"
fi

# Step 9: Display status
print_step "Deployment Status:"
echo "===================="
kubectl get pods -n $NAMESPACE
echo ""
kubectl get svc -n $NAMESPACE

# Step 10: Access information
echo ""
print_step "Access Information:"
echo "===================="
echo "Frontend: kubectl port-forward svc/frontend -n $NAMESPACE 4200:80"
echo "API Gateway: kubectl port-forward svc/api-gateway -n $NAMESPACE 9000:9000"
echo "Grafana: kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80"
echo "Prometheus: kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090"
echo ""
echo "External IPs (if LoadBalancer):"
kubectl get svc -n $NAMESPACE -o wide | grep LoadBalancer

echo ""
print_step "✅ Deployment completed successfully!"
echo "Run 'kubectl get pods -n $NAMESPACE' to check status"
