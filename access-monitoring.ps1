# CNPM Food - Monitoring Access Script
# Usage: .\access-monitoring.ps1 [grafana|prometheus|all]

param(
    [Parameter(Position=0)]
    [ValidateSet("grafana", "prometheus", "all")]
    [string]$Service = "all"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   CNPM Food - Monitoring Access" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

function Start-PortForwardWindow {
    param(
        [string]$ServiceName,
        [string]$Namespace,
        [int]$LocalPort,
        [int]$ServicePort,
        [string]$Url,
        [string]$Description
    )
    
    Write-Host "🚀 Starting $Description..." -ForegroundColor Green
    Write-Host "   URL: $Url" -ForegroundColor Yellow
    Write-Host ""
    
    # Create PowerShell command
    $cmd = "Write-Host '🚀 $Description' -ForegroundColor Green; " +
           "Write-Host 'URL: $Url' -ForegroundColor Cyan; " +
           "Write-Host '`nPress Ctrl+C to stop`n' -ForegroundColor Yellow; " +
           "kubectl port-forward -n $Namespace svc/$ServiceName ${LocalPort}:${ServicePort}"
    
    # Start in new window
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
    
    Start-Sleep -Seconds 2
    
    # Open browser
    Start-Process $Url
}

# Show current status
Write-Host "📊 Monitoring Stack Status:" -ForegroundColor Cyan
Write-Host ""
kubectl get pods -n monitoring
Write-Host ""
kubectl get svc -n monitoring
Write-Host ""

if ($Service -eq "grafana" -or $Service -eq "all") {
    Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "   Starting Grafana" -ForegroundColor Cyan
    Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📊 Grafana Dashboards:" -ForegroundColor Green
    Write-Host "   1. CNPM Food Services (Basic)" -ForegroundColor White
    Write-Host "   2. CNPM Food - Microservices Real-time Monitoring ⭐" -ForegroundColor White
    Write-Host ""
    Write-Host "🔑 Login Credentials:" -ForegroundColor Green
    Write-Host "   Username: admin" -ForegroundColor White
    Write-Host "   Password: admin" -ForegroundColor White
    Write-Host ""
    
    Start-PortForwardWindow -ServiceName "grafana" -Namespace "monitoring" `
        -LocalPort 3000 -ServicePort 3000 `
        -Url "http://localhost:3000" `
        -Description "Grafana Dashboard"
    
    Start-Sleep -Seconds 2
}

if ($Service -eq "prometheus" -or $Service -eq "all") {
    Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "   Starting Prometheus" -ForegroundColor Cyan
    Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📈 Prometheus Features:" -ForegroundColor Green
    Write-Host "   • Targets: http://localhost:9090/targets" -ForegroundColor White
    Write-Host "   • Graph: http://localhost:9090/graph" -ForegroundColor White
    Write-Host "   • Alerts: http://localhost:9090/alerts" -ForegroundColor White
    Write-Host ""
    
    Start-PortForwardWindow -ServiceName "prometheus" -Namespace "monitoring" `
        -LocalPort 9090 -ServicePort 9090 `
        -Url "http://localhost:9090/targets" `
        -Description "Prometheus Monitoring"
    
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host "   ✅ Monitoring is now accessible!" -ForegroundColor Green
Write-Host "════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host ""
Write-Host "📖 Documentation:" -ForegroundColor Cyan
Write-Host "   • MONITORING-QUICKSTART.md - Quick start guide" -ForegroundColor White
Write-Host "   • MONITORING.md - Full documentation" -ForegroundColor White
Write-Host ""
Write-Host "💡 Tip: Port-forwards are running in separate windows." -ForegroundColor Yellow
Write-Host "    Close those windows to stop port-forwarding." -ForegroundColor Yellow
Write-Host ""
Write-Host "Press Enter to exit this script..." -ForegroundColor Gray
Read-Host
