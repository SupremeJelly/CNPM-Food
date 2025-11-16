# 🚀 CI/CD Pipeline Migration Checklist

## Pre-Migration

- [ ] **Backup existing workflows**
  ```powershell
  New-Item -ItemType Directory -Path .github/workflows/backup -Force
  Copy-Item .github/workflows/*.yml .github/workflows/backup/
  ```

- [ ] **Verify required secrets exist**
  - [ ] `DOCKER_USERNAME` - Docker Hub username
  - [ ] `DOCKER_TOKEN` - Docker Hub access token
  - [ ] `KUBE_CONFIG_DATA` - Base64 kubeconfig

  **Generate KUBE_CONFIG_DATA:**
  ```powershell
  $config = Get-Content -Path $HOME\.kube\config -Raw
  $base64 = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($config))
  Write-Output $base64 | clip
  # Now paste into GitHub Secrets
  ```

- [ ] **Test current deployment works**
  ```powershell
  kubectl get pods -n cnpm-food
  kubectl get svc -n cnpm-food
  ```

## Migration Steps

### Option A: Complete Replacement (Recommended for New Projects)

- [ ] **Step 1: Disable old workflows**
  ```powershell
  # Rename to prevent triggering
  Move-Item .github/workflows/ci.yml .github/workflows/ci.yml.disabled
  Move-Item .github/workflows/deploy-k8s.yml .github/workflows/deploy-k8s.yml.disabled
  ```

- [ ] **Step 2: Activate new workflow**
  ```powershell
  # Already created as integrated-cicd.yml
  # Optionally rename:
  Move-Item .github/workflows/integrated-cicd.yml .github/workflows/ci-cd-pipeline.yml
  ```

- [ ] **Step 3: Commit and push**
  ```powershell
  git add .github/workflows/
  git commit -m "feat: migrate to integrated CI/CD pipeline with quality gates"
  git push origin main
  ```

- [ ] **Step 4: Monitor first run**
  - Go to GitHub Actions tab
  - Watch workflow progress
  - Verify all stages complete successfully

### Option B: Gradual Migration (Recommended for Production)

- [ ] **Step 1: Keep old workflows as backup**
  ```powershell
  Move-Item .github/workflows/ci.yml .github/workflows/ci-legacy.yml
  Move-Item .github/workflows/deploy-k8s.yml .github/workflows/deploy-legacy.yml
  ```

- [ ] **Step 2: Create feature branch for testing**
  ```powershell
  git checkout -b feat/integrated-cicd
  git add .github/workflows/integrated-cicd.yml
  git commit -m "feat: add integrated CI/CD pipeline"
  git push origin feat/integrated-cicd
  ```

- [ ] **Step 3: Test on feature branch**
  - Create PR from `feat/integrated-cicd` to `main`
  - Verify all tests run
  - Check that deployment is skipped (PR mode)

- [ ] **Step 4: Merge and test full deployment**
  - Merge PR to main
  - Monitor deployment to K8s
  - Verify services are running

- [ ] **Step 5: Disable legacy workflows after success**
  ```powershell
  git rm .github/workflows/ci-legacy.yml
  git rm .github/workflows/deploy-k8s.yml
  git commit -m "chore: remove legacy CI/CD workflows"
  git push origin main
  ```

## Post-Migration Validation

### Workflow Verification

- [ ] **Check GitHub Actions page**
  - Latest workflow shows green ✅
  - All 6 stages completed
  - No failed jobs

- [ ] **Verify artifacts uploaded**
  - Go to workflow run → Artifacts section
  - Should see: `backend-jars`, `frontend-dist`, `test-results-*`

- [ ] **Check Docker Hub**
  ```powershell
  # Verify images were pushed with SHA tags
  # https://hub.docker.com/r/onlykohi/cnpm-user-service/tags
  ```

### Deployment Verification

- [ ] **Check pod status**
  ```powershell
  kubectl get pods -n cnpm-food
  # All pods should be Running with 1/1 READY
  ```

- [ ] **Verify image tags**
  ```powershell
  kubectl get deployment -n cnpm-food -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.spec.template.spec.containers[0].image}{"\n"}{end}'
  # Should show SHA-tagged images (not :latest)
  ```

- [ ] **Test service endpoints**
  ```powershell
  # Port-forward and test
  kubectl port-forward svc/frontend 8080:80 -n cnpm-food
  # Open http://localhost:8080
  ```

- [ ] **Check monitoring**
  ```powershell
  kubectl get pods -n monitoring
  # grafana and prometheus should be Running
  
  # Access monitoring
  kubectl port-forward svc/grafana 3000:3000 -n monitoring
  # Open http://localhost:3000 (admin/admin)
  ```

### Functional Testing

- [ ] **Pull Request workflow**
  ```powershell
  git checkout -b test/pr-workflow
  # Make a small change
  echo "# Test" >> README.md
  git add README.md
  git commit -m "test: PR workflow"
  git push origin test/pr-workflow
  # Create PR and verify:
  # - Tests run ✅
  # - Code quality checks ✅
  # - Build artifacts ✅
  # - Deployment SKIPPED (PR mode) ✅
  ```

- [ ] **Main branch deployment**
  ```powershell
  # Merge test PR to main
  # Verify:
  # - All stages run ✅
  # - Images built and pushed ✅
  # - Deployed to K8s ✅
  # - Monitoring updated ✅
  ```

- [ ] **Changed service detection**
  ```powershell
  # Make change to single service
  git checkout -b test/change-detection
  echo "// test" >> user-service/src/main/java/com/example/Main.java
  git add user-service/
  git commit -m "test: change detection for user-service"
  git push origin test/change-detection
  # Merge to main and verify:
  # - Only user-service image rebuilt ✅
  # - Other services use cached images ✅
  # - Deployment faster (~18 min vs ~30 min) ✅
  ```

## Rollback Plan (If Issues Occur)

- [ ] **Immediate rollback**
  ```powershell
  # Re-enable old workflows
  git mv .github/workflows/ci-legacy.yml .github/workflows/ci.yml
  git mv .github/workflows/deploy-legacy.yml .github/workflows/deploy-k8s.yml
  git rm .github/workflows/integrated-cicd.yml
  git commit -m "revert: rollback to legacy CI/CD"
  git push origin main
  ```

- [ ] **Manual deployment (emergency)**
  ```powershell
  # If workflows broken, deploy manually:
  cd user-service
  docker build -t onlykohi/cnpm-user-service:emergency .
  docker push onlykohi/cnpm-user-service:emergency
  kubectl set image deployment/user-service user-service=onlykohi/cnpm-user-service:emergency -n cnpm-food
  ```

## Troubleshooting

### Issue: Workflow doesn't trigger

**Check:**
- [ ] Workflow file is in `.github/workflows/` (not `backup/`)
- [ ] File extension is `.yml` (not `.yml.disabled`)
- [ ] Push was to `main` or `src` branch

**Fix:**
```powershell
git checkout main
git pull origin main
ls .github/workflows/  # Verify integrated-cicd.yml exists
```

### Issue: "Secrets required but not provided"

**Check:**
- [ ] Go to repo Settings → Secrets and variables → Actions
- [ ] Verify all 3 secrets exist: `DOCKER_USERNAME`, `DOCKER_TOKEN`, `KUBE_CONFIG_DATA`

**Fix:**
```powershell
# Re-generate KUBE_CONFIG_DATA
$config = Get-Content -Path $HOME\.kube\config -Raw
$base64 = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($config))
Write-Output $base64
# Add to GitHub Secrets
```

### Issue: Image push denied

**Check:**
- [ ] Docker Hub username matches `DOCKER_USERNAME` secret
- [ ] `DOCKER_TOKEN` is an access token (not password)
- [ ] Token has `Read, Write, Delete` permissions

**Fix:**
```powershell
# Generate new token at https://hub.docker.com/settings/security
# Copy token
# Update DOCKER_TOKEN secret in GitHub
```

### Issue: kubectl fails to connect

**Check:**
- [ ] `KUBE_CONFIG_DATA` is correctly base64-encoded
- [ ] Kubernetes cluster is running (Docker Desktop → Settings → Kubernetes)
- [ ] Kubeconfig context is correct

**Fix:**
```powershell
# Test locally first
kubectl cluster-info
kubectl get nodes

# If works, re-encode config
$config = Get-Content -Path $HOME\.kube\config -Raw
$base64 = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($config))
# Update KUBE_CONFIG_DATA secret
```

### Issue: Deployment timeout

**Check:**
- [ ] Download diagnostics artifact from failed workflow
- [ ] Check pod logs in artifact files
- [ ] Review `kubectl describe pod` output

**Common Causes:**
- MySQL not ready → initContainers should handle this
- Image pull error → Check Docker Hub, verify image exists
- Resource limits too low → Increase in deployment YAML
- Probe timing → Adjust startupProbe failureThreshold

**Fix:**
```powershell
# Check pod status
kubectl get pods -n cnpm-food
kubectl describe pod <pod-name> -n cnpm-food
kubectl logs <pod-name> -n cnpm-food --tail=100

# Force pull new image
kubectl delete pod -l app=user-service -n cnpm-food
```

### Issue: Tests fail in CI but pass locally

**Check:**
- [ ] Environment variables differ (CI uses secrets, local uses .env)
- [ ] Database state (CI uses fresh MySQL, local may have stale data)
- [ ] Dependency versions (check `package-lock.json` / `pom.xml` up to date)

**Fix:**
```powershell
# Ensure lock files are committed
git add frontend/package-lock.json
git add */pom.xml
git commit -m "chore: update lock files"
git push origin main
```

## Success Criteria

### All checks must pass:

✅ **Workflow Execution**
- [ ] All 6 stages complete successfully
- [ ] Total time < 35 minutes
- [ ] Artifacts uploaded (backend-jars, frontend-dist)

✅ **Deployment**
- [ ] All pods Running (1/1 READY)
- [ ] Images tagged with commit SHA
- [ ] Services accessible via port-forward

✅ **Monitoring**
- [ ] Grafana accessible at localhost:3000
- [ ] Prometheus scraping all 5 services
- [ ] Dashboard shows real-time metrics

✅ **Quality Gates**
- [ ] PR tests run without deployment
- [ ] Failed tests block deployment
- [ ] Code quality checks enforce standards

✅ **Performance**
- [ ] Change detection works (only modified services rebuilt)
- [ ] Docker cache utilized (subsequent runs faster)
- [ ] No redundant builds (JARs built once)

## Final Steps

- [ ] **Update documentation**
  ```powershell
  # Ensure README.md references new workflow
  # Add badge to show build status
  ```

- [ ] **Team notification**
  - Inform team about new CI/CD pipeline
  - Share `CI-CD-INTEGRATION.md` documentation
  - Update deployment procedures

- [ ] **Monitor for 1 week**
  - Track deployment success rate
  - Collect feedback from team
  - Optimize based on usage patterns

- [ ] **Delete backup workflows** (after 1 week success)
  ```powershell
  Remove-Item -Recurse -Force .github/workflows/backup/
  ```

## 📚 Reference Documentation

- **Integration Guide**: `CI-CD-INTEGRATION.md`
- **Architecture Diagrams**: `CI-CD-ARCHITECTURE.md`
- **Monitoring Setup**: `MONITORING.md`
- **Access Instructions**: `HOW-TO-ACCESS-MONITORING.md`

---

**Last Updated**: $(Get-Date -Format "yyyy-MM-dd")  
**Migration Owner**: DevOps Team  
**Rollback Contact**: Senior DevOps Engineer
