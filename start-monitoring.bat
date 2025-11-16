@echo off
REM CNPM Food - Quick Monitoring Access
REM Double-click this file to start monitoring

echo ========================================
echo    CNPM Food - Starting Monitoring
echo ========================================
echo.

REM Check if kubectl is available
kubectl version --client >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: kubectl not found!
    echo Please install kubectl first.
    pause
    exit /b 1
)

echo Checking monitoring pods...
echo.
kubectl get pods -n monitoring
echo.

echo Starting Grafana and Prometheus...
echo.

REM Start Grafana port-forward in new window
start "Grafana Port-Forward" cmd /k "echo Grafana is running at http://localhost:3000 && echo Login: admin/admin && echo. && echo Press Ctrl+C to stop && kubectl port-forward -n monitoring svc/grafana 3000:3000"

REM Wait a bit
timeout /t 2 /nobreak >nul

REM Start Prometheus port-forward in new window
start "Prometheus Port-Forward" cmd /k "echo Prometheus is running at http://localhost:9090 && echo Targets: http://localhost:9090/targets && echo. && echo Press Ctrl+C to stop && kubectl port-forward -n monitoring svc/prometheus 9090:9090"

REM Wait for port-forwards to start
echo Waiting for services to start...
timeout /t 5 /nobreak >nul

REM Open browsers
echo Opening Grafana in browser...
start http://localhost:3000

timeout /t 2 /nobreak >nul

echo Opening Prometheus in browser...
start http://localhost:9090/targets

echo.
echo ========================================
echo    SUCCESS!
echo ========================================
echo.
echo Grafana:    http://localhost:3000
echo            Username: admin
echo            Password: admin
echo.
echo Prometheus: http://localhost:9090
echo.
echo Port-forwards are running in separate windows.
echo Close those windows to stop monitoring.
echo.
echo Press any key to close this window...
pause >nul
