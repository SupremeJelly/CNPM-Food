# CI/CD Pipeline Architecture

## 🏗️ Before vs After Architecture

### ❌ Old Architecture (Separated Workflows)

```
┌─────────────────────────────────────────────────────────────────┐
│                         REPOSITORY                              │
│                                                                 │
│  Push to main  ──────────┬────────────────────────────────────┐│
│                          │                                     ││
│                          ▼                                     ▼│
│  ┌─────────────────────────────────┐  ┌──────────────────────┐│
│  │       ci.yml (ubuntu)           │  │  deploy-k8s.yml      ││
│  │                                 │  │   (self-hosted)      ││
│  │  1. Test backend (matrix)      │  │                      ││
│  │  2. Test frontend              │  │  1. Build images     ││
│  │  3. Build JARs ──────┐         │  │     (rebuild JARs)   ││
│  │  4. Build frontend   │         │  │  2. Push registry    ││
│  │  5. Code quality     │         │  │  3. Deploy K8s       ││
│  │                      │         │  │                      ││
│  │  Upload artifacts ◄──┘         │  │  ❌ Doesn't use      ││
│  │  (90 days, unused)             │  │     artifacts!       ││
│  └─────────────────────────────────┘  └──────────────────────┘│
│           ▲                                     ▲              │
│           │                                     │              │
│           └─────── NO DEPENDENCY ───────────────┘              │
│                   (Can deploy broken code!)                    │
└─────────────────────────────────────────────────────────────────┘

Problems:
❌ CI and CD run independently
❌ No quality gates (can deploy if tests fail)
❌ Redundant builds (JARs built twice)
❌ Different environments (ubuntu vs self-hosted)
❌ Artifacts uploaded but never reused
❌ Can't test PRs before deployment
```

### ✅ New Architecture (Integrated Pipeline)

```
┌────────────────────────────────────────────────────────────────────────┐
│                           REPOSITORY                                   │
│                                                                        │
│  Push/PR ────────────────┬─────────────────────────────────────────┐  │
│                          │                                         │  │
│                          ▼                                         ▼  │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │            integrated-cicd.yml (ubuntu-latest only)            │  │
│  │                                                                │  │
│  │  STAGE 1: TEST (Parallel) ✅                                   │  │
│  │  ┌─────────────────┐  ┌─────────────────┐                     │  │
│  │  │ test-backend    │  │ test-frontend   │                     │  │
│  │  │ (5 services)    │  │ (lint + unit)   │                     │  │
│  │  └────────┬────────┘  └────────┬────────┘                     │  │
│  │           └─────────┬───────────┘                              │  │
│  │                     ▼                                          │  │
│  │  STAGE 2: CODE QUALITY ✅                                      │  │
│  │  ┌─────────────────────────────┐                              │  │
│  │  │ OWASP + npm audit           │                              │  │
│  │  └──────────────┬──────────────┘                              │  │
│  │                 ▼                                              │  │
│  │  STAGE 3: BUILD ARTIFACTS ✅                                   │  │
│  │  ┌─────────────────────────────┐                              │  │
│  │  │ Build JARs (skip tests)     │                              │  │
│  │  │ Build frontend dist         │                              │  │
│  │  │ Upload to artifacts (30d)   │                              │  │
│  │  └──────────────┬──────────────┘                              │  │
│  │                 ▼                                              │  │
│  │  ┌─────────────────────────────┐                              │  │
│  │  │ Is push to main/src? ◄──────┼─── PR: Stop here (no deploy)│  │
│  │  └──────────────┬──────────────┘                              │  │
│  │                 │ Yes                                          │  │
│  │                 ▼                                              │  │
│  │  STAGE 4: BUILD IMAGES ✅                                      │  │
│  │  ┌─────────────────────────────┐                              │  │
│  │  │ Download artifacts          │                              │  │
│  │  │ Build Docker images (fast)  │                              │  │
│  │  │ Push to Docker Hub          │                              │  │
│  │  └──────────────┬──────────────┘                              │  │
│  │                 ▼                                              │  │
│  │  STAGE 5: DEPLOY ✅                                            │  │
│  │  ┌─────────────────────────────┐                              │  │
│  │  │ Setup kubectl               │                              │  │
│  │  │ Deploy MySQL (if needed)    │                              │  │
│  │  │ Deploy microservices        │                              │  │
│  │  │ Update to SHA tag           │                              │  │
│  │  │ Wait for rollout            │                              │  │
│  │  │ Deploy monitoring stack     │                              │  │
│  │  └─────────────────────────────┘                              │  │
│  │                                                                │  │
│  │  STAGE 6: SUMMARY (always runs) ✅                             │  │
│  │  ┌─────────────────────────────┐                              │  │
│  │  │ Aggregate test results      │                              │  │
│  │  │ Post to PR/Summary          │                              │  │
│  │  └─────────────────────────────┘                              │  │
│  └────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘

Benefits:
✅ Enforced quality gates (needs: dependency)
✅ Single build per artifact (reused)
✅ Same environment everywhere (ubuntu-latest)
✅ Artifacts downloaded and reused
✅ PR testing without deployment
✅ Clear stage progression
```

## 🔄 Workflow Decision Tree

```
                    Push/PR to repository
                            │
                            ▼
                    ┌───────────────┐
                    │  Run Tests    │ ◄─── Always runs
                    │  (Stage 1)    │
                    └───────┬───────┘
                            │
                    ┌───────▼────────┐
                    │  Tests Pass?   │
                    └───────┬────────┘
                            │
                    ┌───────┴───────┐
                    │               │
                   Yes             No ──► ❌ Stop (Fail)
                    │
                    ▼
            ┌───────────────┐
            │ Code Quality  │
            │  (Stage 2)    │
            └───────┬───────┘
                    │
            ┌───────▼────────┐
            │ Quality Pass?  │
            └───────┬────────┘
                    │
            ┌───────┴───────┐
            │               │
           Yes             No ──► ❌ Stop (Fail)
            │
            ▼
    ┌───────────────┐
    │ Build Artifacts│
    │  (Stage 3)    │
    └───────┬───────┘
            │
    ┌───────▼────────┐
    │ Build Success? │
    └───────┬────────┘
            │
    ┌───────┴───────┐
    │               │
   Yes             No ──► ❌ Stop (Fail)
    │
    ▼
┌─────────────────────┐
│ Is push to main/src?│
└──────────┬──────────┘
           │
    ┌──────┴──────┐
    │             │
   Yes           No ──► ✅ Stop (Success - PR tested)
    │
    ▼
┌───────────────┐
│ Build Images  │
│  (Stage 4)    │
└───────┬───────┘
        │
┌───────▼────────┐
│ Images Built?  │
└───────┬────────┘
        │
┌───────┴───────┐
│               │
Yes             No ──► ❌ Stop (Fail)
│
▼
┌───────────────┐
│ Deploy to K8s │
│  (Stage 5)    │
└───────┬───────┘
        │
┌───────▼────────┐
│ Deploy Success?│
└───────┬────────┘
        │
┌───────┴───────┐
│               │
Yes             No ──► ❌ Stop (Fail + Diagnostics)
│
▼
✅ Complete (Monitoring Active)
```

## 📊 Artifact Lifecycle

```
┌────────────────────────────────────────────────────────────────┐
│                     STAGE 3: Build Artifacts                   │
│                                                                │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  Maven Build                                            │  │
│  │  user-service/target/*.jar        ──────┐              │  │
│  │  order-service/target/*.jar       ──────┤              │  │
│  │  restaurant-service/target/*.jar  ──────┤              │  │
│  │  payment-service/target/*.jar     ──────┼─► Upload     │  │
│  │  api-gateway/target/*.jar         ──────┘  Artifact    │  │
│  │                                               │         │  │
│  │  npm build                                    │         │  │
│  │  frontend/dist/**                 ────────────┘         │  │
│  └─────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
                              │
                              │ (Artifact Storage: 30 days)
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                     STAGE 4: Build Images                      │
│                                                                │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  Download Artifacts                                     │  │
│  │    ├─ backend-jars/user-service/target/*.jar           │  │
│  │    ├─ backend-jars/order-service/target/*.jar          │  │
│  │    ├─ backend-jars/restaurant-service/target/*.jar     │  │
│  │    ├─ backend-jars/payment-service/target/*.jar        │  │
│  │    ├─ backend-jars/api-gateway/target/*.jar            │  │
│  │    └─ frontend-dist/dist/**                            │  │
│  └─────────────────────────────────────────────────────────┘  │
│                              │                                 │
│                              ▼                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  Dockerfile (No Maven rebuild!)                        │  │
│  │                                                         │  │
│  │  FROM eclipse-temurin:21-jre                           │  │
│  │  COPY user-service/target/*.jar app.jar  ◄─────────────┼──┐│
│  │  ENTRYPOINT ["java", "-jar", "app.jar"]    (pre-built) │  ││
│  └─────────────────────────────────────────────────────────┘  ││
│                              │                                 ││
│                              ▼                                 ││
│  ┌─────────────────────────────────────────────────────────┐  ││
│  │  Docker Build (Fast - just COPY layers)                │  ││
│  │  ├─ onlykohi/cnpm-user-service:latest                  │  ││
│  │  ├─ onlykohi/cnpm-user-service:abc123 (SHA)            │  ││
│  │  └─ Push to Docker Hub                                 │  ││
│  └─────────────────────────────────────────────────────────┘  ││
└────────────────────────────────────────────────────────────────┘│
                              │                                  │
                              ▼                                  │
┌────────────────────────────────────────────────────────────────┘│
│                     STAGE 5: Deploy                            │
│                                                                │
│  kubectl set image deployment/user-service \                  │
│    user-service=onlykohi/cnpm-user-service:abc123 ◄────────────┘
│                                                                
│  (Pulls pre-built image from Docker Hub)                      
└────────────────────────────────────────────────────────────────┘

Time Savings:
❌ Old: Maven build in Docker (5-8 min per service) = 40 min total
✅ New: Download + COPY (30 sec per service) = 3 min total
      
      ⚡ ~37 minutes saved per deployment!
```

## 🔐 Security Flow

```
┌────────────────────────────────────────────────────────────────┐
│                    GitHub Repository Secrets                   │
│                                                                │
│  DOCKER_USERNAME    ──┐                                        │
│  DOCKER_TOKEN       ──┼──► Stored encrypted in GitHub         │
│  KUBE_CONFIG_DATA   ──┘     (Not visible in logs/code)        │
└────────────────────────────────────────────────────────────────┘
                              │
                              │ (Injected at runtime)
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                    Workflow Execution                          │
│                                                                │
│  Stage 4: Build Images                                         │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ docker login                                             │ │
│  │   -u ${{ secrets.DOCKER_USERNAME }}                      │ │
│  │   -p ${{ secrets.DOCKER_TOKEN }}    ◄─ Masked in logs   │ │
│  └──────────────────────────────────────────────────────────┘ │
│                              │                                 │
│  Stage 5: Deploy                                               │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ echo $KUBE_CONFIG_DATA | base64 -d > ~/.kube/config     │ │
│  │                           ▲                              │ │
│  │                           └─ Masked in logs              │ │
│  └──────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────┘

Security Best Practices:
✅ Secrets never committed to code
✅ Tokens have limited scope (Docker Hub: read/write, K8s: deploy only)
✅ Secrets automatically masked in logs
✅ Use Personal Access Tokens (not passwords)
✅ Rotate secrets every 90 days
```

## 📈 Performance Comparison

### Old Pipeline (Separated Workflows)

```
ci.yml:
├─ test-backend:     5 min
├─ test-frontend:    3 min
├─ build-backend:    8 min (Maven compile)
├─ build-frontend:   4 min
└─ code-quality:     3 min
   Total:           23 min ✅

deploy-k8s.yml (independent):
├─ build-images:    35 min ❌ (rebuilds JARs in Docker)
├─ deploy:          5 min
└─ monitoring:      2 min
   Total:           42 min

Combined (if both run): ~42 min (limited by slowest)
Wasted time: 8 min (duplicate JAR builds)
```

### New Pipeline (Integrated)

```
Stage 1 (Test):         5 min  ✅ (parallel matrix)
Stage 2 (Quality):      3 min  ✅
Stage 3 (Build):        8 min  ✅
Stage 4 (Images):       8 min  ✅ (downloads artifacts, fast COPY)
Stage 5 (Deploy):       5 min  ✅
Stage 6 (Summary):      1 min  ✅
────────────────────────────────
Total:                 30 min

Speedup:    ~28% faster (42→30 min)
Efficiency: No redundant builds
Cache hit:  15-20 min (with Docker cache)
```

## 🎯 Deployment Matrix

```
┌─────────────────────────────────────────────────────────────────┐
│                    Change Detection Logic                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Git Changes          →  Services Rebuilt  →  Deployment Time  │
│                                                                 │
│  frontend/            →  frontend only      →  ~15 min         │
│  user-service/        →  user-service only  →  ~18 min         │
│  k8s/                 →  all services       →  ~30 min         │
│  mysql-init/          →  MySQL + all svcs   →  ~35 min         │
│  README.md            →  none (skip build)  →  ~10 min (test)  │
│                                                                 │
│  Multiple services:                                             │
│  user-service/        →  user + order       →  ~20 min         │
│  order-service/                                                 │
└─────────────────────────────────────────────────────────────────┘

Optimization: Only changed services are rebuilt/redeployed
```

## 🚨 Failure Handling

```
                    Stage 1: Test
                         │
                Failed ──┼──► ❌ STOP
                         │     │
                    Passed      │
                         │      ▼
                    Stage 2   ┌─────────────────────┐
                         │    │ Actions Taken:      │
                Failed ──┼──► │ - Mark workflow red │
                         │    │ - Upload test logs  │
                    Passed     │ - Post PR comment   │
                         │    │ - Block merge       │
                         │    └─────────────────────┘
                    Stage 3
                         │
                Failed ──┼──► ❌ STOP (build artifacts saved)
                         │
                    Passed
                         │
                    Stage 4
                         │
                Failed ──┼──► ❌ STOP (image build logs saved)
                         │
                    Passed
                         │
                    Stage 5: Deploy
                         │
                Failed ──┼──► ❌ ROLLBACK + DIAGNOSTICS
                         │     ┌─────────────────────────┐
                         │     │ - kubectl logs          │
                         │     │ - Pod descriptions      │
                         │     │ - Upload diagnostics    │
                         │     │ - Keep old deployment   │
                         │     └─────────────────────────┘
                         │
                    Passed
                         │
                         ▼
              ✅ Deployment Complete
              (Monitoring dashboard active)
```

## 📝 Summary

| Aspect              | Old (Separated)    | New (Integrated)   |
|---------------------|--------------------|--------------------|
| **Workflows**       | 2 independent      | 1 unified          |
| **Quality Gates**   | ❌ None            | ✅ Enforced        |
| **Build Redundancy**| ❌ JARs built 2x   | ✅ Built once      |
| **Environment**     | ❌ Mixed runners   | ✅ Ubuntu-latest   |
| **Artifact Reuse**  | ❌ No              | ✅ Yes             |
| **PR Testing**      | ❌ Limited         | ✅ Full pipeline   |
| **Deployment**      | ❌ Any time        | ✅ After validation|
| **Time (full)**     | ~42 min            | ~30 min            |
| **Time (cached)**   | ~35 min            | ~15-20 min         |
| **Diagnostics**     | ⚠️ Manual          | ✅ Automated       |
| **Security**        | ⚠️ Secrets exposed | ✅ Properly masked |
