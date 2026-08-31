@echo off
echo ==================================================
echo  Starting Food Delivery Microservices Suite...
echo ==================================================
echo Starting Kafka and Zookeeper...
docker compose up -d zookeeper kafka

start "Auth Service (8081)" cmd /k "cd services\auth-service && mvn spring-boot:run"
start "Order Service (8082)" cmd /k "cd services\order-service && mvn spring-boot:run"
start "Restaurant Service (8083)" cmd /k "cd services\restaurant-service && mvn spring-boot:run"
start "Delivery Service (8084)" cmd /k "cd services\delivery-service && mvn spring-boot:run"
start "API Gateway (8090)" cmd /k "cd services\api-gateway && mvn spring-boot:run"

echo All 5 services are launching!
echo Unified Swagger UI will be available at: http://localhost:8090/swagger-ui.html
