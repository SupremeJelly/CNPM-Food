Grafana Alloy (Windows / winget) configuration

This folder contains configuration files you can use when installing Grafana Alloy via `winget` on Windows.

What is included:
- `grafana.ini` - minimal grafana config (example)
- `provisioning/datasources.yaml` - Loki datasource example
- `provisioning/dashboards.yaml` - dashboard provider configured for default Grafana install path
- `dashboards/example-dashboard.json` - small example dashboard

Instructions (summary):

1) Install Grafana Alloy with winget (example; replace package id with the one you use):

```powershell
winget install --id Grafana.GrafanaAlloy -e
```

If you install the OSS Grafana for testing, the package id may be `Grafana.Grafana`.

2) Locate Grafana installation folder

Common install paths:
- `C:\Program Files\GrafanaLabs\grafana` (OSS)
- `C:\Program Files\Grafana Alloy` or similar for Alloy distribution

If you are unsure, run in PowerShell (as admin) to find the service file path:

```powershell
Get-Service -Name grafana | Select-Object -Property Name, Status
# or inspect the binary path
(Get-ItemProperty "HKLM:\SYSTEM\CurrentControlSet\Services\grafana").ImagePath
```

3) Copy provisioning files and dashboards into Grafana data folders

Adjust target paths to match your Grafana install. Example target paths (replace if different):

```powershell
$GrafanaRoot = "C:\\Program Files\\GrafanaLabs\\grafana"
Copy-Item .\provisioning\datasources.yaml "$GrafanaRoot\conf\provisioning\datasources\"
Copy-Item .\provisioning\dashboards.yaml "$GrafanaRoot\conf\provisioning\dashboards\"
Copy-Item .\dashboards\example-dashboard.json "$GrafanaRoot\data\dashboards\"
Copy-Item .\grafana.ini "$GrafanaRoot\conf\"
```

4) Set Alloy license (if applicable)

Grafana Alloy requires a license/token. Set it as a system environment variable `GF_ENTERPRISE_LICENSE` (replace value).

```powershell
setx GF_ENTERPRISE_LICENSE "<YOUR_ALLOY_LICENSE>" /M
```

5) Restart Grafana service

```powershell
Restart-Service -Name grafana
```

6) Open Grafana UI: http://localhost:3000 (login: admin / password from `GF_SECURITY_ADMIN_PASSWORD` if set)

Notes and recommendations:
- Running Grafana as a Windows service means file copy operations should be done as Administrator.
- If you need to set additional Alloy-specific environment variables, set them via `setx` and restart the service.
- For production, consider using Grafana official installers or orchestration suited to your environment.

If you want, I can:
- Update the `provisioning/dashboards.yaml` path to whichever path your installation uses (tell me the Grafana root path), or
- Copy these files into your local Grafana installation automatically (I can run PowerShell commands here if you want).
 
Using the helper:

1. Edit `config.alloy` and add your values.
2. Run PowerShell as Administrator and execute:

```powershell
cd grafana-windows-config
.\apply-config.ps1 -ConfigFile .\config.alloy
```

The script will set system env vars, copy provisioning and dashboard files into the detected Grafana installation, and restart the `grafana` service.