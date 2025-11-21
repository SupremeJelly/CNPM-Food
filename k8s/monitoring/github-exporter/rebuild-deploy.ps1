<#
Simple helper: build image, update deployment, and show debug info on failure.
Usage (from repo root):
  powershell -File k8s\monitoring\github-exporter\rebuild-deploy.ps1
Options:
  -Image <image>    : full image name (default: ghcr.io/you/github-exporter:<git-short-sha> or :latest)
  -Push             : push image after build
#>
param(
  [string]$Image = "",
  [switch]$Push
)

Set-StrictMode -Version Latest
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition

# Calculate repo root relative to this script (k8s/monitoring/github-exporter -> repo root is three levels up)
$repoRoot = Resolve-Path (Join-Path $scriptDir '..\..\..')
Push-Location $repoRoot

if (-not $Image) {
    # try to get git short sha
    try {
        $sha = git rev-parse --short HEAD 2>$null
        if ($LASTEXITCODE -eq 0 -and $sha) { $Image = "ghcr.io/you/github-exporter:$sha" }
        else { $Image = "ghcr.io/you/github-exporter:latest" }
    } catch {
        $Image = "ghcr.io/you/github-exporter:latest"
    }
}

Write-Host "Building image: $Image"
# Build using the script directory as the docker context (contains Dockerfile)
docker build -t $Image $scriptDir
if ($LASTEXITCODE -ne 0) { Write-Host 'Docker build failed'; Exit 1 }

if ($Push) {
    Write-Host "Pushing $Image"
    docker push $Image
    if ($LASTEXITCODE -ne 0) { Write-Host 'Docker push failed'; Exit 1 }
}

Write-Host 'Applying k8s manifest (no-op if unchanged)'
kubectl apply -f k8s/monitoring/github-exporter.yaml -n monitoring

Write-Host "Updating deployment image to $Image"
kubectl set image deployment/github-exporter exporter=$Image -n monitoring --record

Write-Host 'Waiting for rollout...'
kubectl rollout status deployment/github-exporter -n monitoring --timeout=120s
$rolloutExit = $LASTEXITCODE

if ($rolloutExit -ne 0) {
    Write-Host "Rollout failed (exit $rolloutExit). Gathering debug info..." -ForegroundColor Red
    kubectl get deploy -n monitoring
    kubectl get rs -n monitoring
    kubectl get pods -n monitoring -o wide
    $pod = kubectl get pods -n monitoring -l app=github-exporter -o jsonpath='{.items[0].metadata.name}' 2>$null
    if ($pod) {
        Write-Host "Describing pod $pod"
        kubectl describe pod $pod -n monitoring
        Write-Host "Recent events for pod (last 50)"
        kubectl get events -n monitoring --sort-by='.lastTimestamp' | Select-Object -Last 50
        Write-Host "Logs (if any):"
        kubectl logs $pod -n monitoring --tail=200
    } else {
        Write-Host 'No github-exporter pod found.'
    }
    Exit $rolloutExit
}

Write-Host 'Rollout successful.' -ForegroundColor Green

Pop-Location
