# Start all Food Delivery microservices concurrently
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " Starting Food Delivery Microservices Suite...     " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

$root = $PSScriptRoot

# Ensure Kafka & Zookeeper are running in Docker
Write-Host "[0/6] Starting Kafka & Zookeeper containers..." -ForegroundColor Yellow
docker compose up -d zookeeper kafka

# Start Auth Service (8081)
Write-Host "[1/6] Starting Auth Service on port 8081..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\services\auth-service'; Write-Host '--- AUTH SERVICE (8081) ---' -ForegroundColor Yellow; mvn spring-boot:run"

# Start Order Service (8082)
Write-Host "[2/6] Starting Order Service on port 8082..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\services\order-service'; Write-Host '--- ORDER SERVICE (8082) ---' -ForegroundColor Yellow; mvn spring-boot:run"

# Start Restaurant Service (8083)
Write-Host "[3/6] Starting Restaurant Service on port 8083..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\services\restaurant-service'; Write-Host '--- RESTAURANT SERVICE (8083) ---' -ForegroundColor Yellow; mvn spring-boot:run"

# Start Delivery Service (8084)
Write-Host "[4/6] Starting Delivery Service on port 8084..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\services\delivery-service'; Write-Host '--- DELIVERY SERVICE (8084) ---' -ForegroundColor Yellow; mvn spring-boot:run"

# Start Payment Service (8085)
Write-Host "[5/7] Starting Payment Service on port 8085..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\services\payment-service'; Write-Host '--- PAYMENT SERVICE (8085) ---' -ForegroundColor Yellow; mvn spring-boot:run"

# Start Search Service (8086)
Write-Host "[6/7] Starting Search Service on port 8086..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\services\search-service'; Write-Host '--- SEARCH SERVICE (8086) ---' -ForegroundColor Yellow; mvn spring-boot:run"

# Start API Gateway (8090)
Write-Host "[7/7] Starting API Gateway & Swagger Hub on port 8090..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\services\api-gateway'; Write-Host '--- API GATEWAY (8090) ---' -ForegroundColor Yellow; mvn spring-boot:run"

Write-Host "`nAll 7 services are launching in parallel!" -ForegroundColor Cyan
Write-Host "Unified Swagger UI: http://localhost:8090/swagger-ui.html" -ForegroundColor Yellow
Write-Host "To stop all running services at once, run: .\stop-all.ps1`n" -ForegroundColor DarkGray
