Write-Host "This script has moved to 'k8s/monitoring/github-exporter/rebuild-deploy.ps1'."
Write-Host "Calling the new script to keep compatibility..."

# Compute path to new script (relative from this script: ../../k8s/monitoring/github-exporter/rebuild-deploy.ps1)
$newScript = Join-Path $PSScriptRoot '..\..\k8s\monitoring\github-exporter\rebuild-deploy.ps1'
$resolved = Resolve-Path $newScript -ErrorAction SilentlyContinue
if (-not $resolved) {
    Write-Host "Cannot find the new script at $newScript. Please run k8s/monitoring/github-exporter/rebuild-deploy.ps1 instead." -ForegroundColor Yellow
    Exit 2
}

& $resolved.Path @Args
Pop-Location
