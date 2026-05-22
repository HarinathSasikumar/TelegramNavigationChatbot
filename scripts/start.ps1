# start.ps1 — Clears port 8085 then starts the Spring Boot server
$PORT = 8085

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  WhatsApp Navigation Chatbot - Starting Server  " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

$lines = netstat -ano | Select-String ":$PORT\s"
$pids  = $lines | ForEach-Object { ($_.Line -split '\s+')[-1] } |
         Where-Object { $_ -match '^\d+$' -and [int]$_ -gt 0 } |
         Sort-Object -Unique

if ($pids.Count -gt 0) {
    Write-Host "  [!] Port $PORT in use - terminating PID(s): $($pids -join ', ')" -ForegroundColor Yellow
    foreach ($p in $pids) {
        taskkill /PID $p /F 2>&1 | Out-Null
    }
    Start-Sleep -Milliseconds 800
    Write-Host "  [OK] Port $PORT cleared." -ForegroundColor Green
} else {
    Write-Host "  [OK] Port $PORT is free." -ForegroundColor Green
}

Write-Host ""
Write-Host "  Loading .env Configuration ..." -ForegroundColor Cyan
if (Test-Path ".env") {
    Get-Content ".env" | Where-Object { $_ -match '^[^#]' -and $_ -match '=' } | ForEach-Object {
        $name, $value = $_ -split '=', 2
        $name = $name.Trim()
        $value = $value.Trim()
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
    Write-Host "  [OK] Environment variables loaded." -ForegroundColor Green
} else {
    Write-Host "  [!] No .env file found. Proceeding with system defaults." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "  Launching Spring Boot on port $PORT ..." -ForegroundColor Cyan
Write-Host ""

mvn spring-boot:run
