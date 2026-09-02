# Stop all running microservices by port (8081, 8082, 8083, 8084, 8085, 8090)
Write-Host "Stopping all Food Delivery microservices..." -ForegroundColor Yellow

$ports = @(8081, 8082, 8083, 8084, 8085, 8090)

foreach ($port in $ports) {
    $processes = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
    if ($processes) {
        foreach ($pidToKill in $processes) {
            Write-Host "Stopping process on port $port (PID: $pidToKill)..." -ForegroundColor Red
            Stop-Process -Id $pidToKill -Force -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "Port $port is already free." -ForegroundColor Green
    }
}

Write-Host "All services stopped." -ForegroundColor Cyan
