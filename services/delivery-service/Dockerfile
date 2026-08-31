FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml /app/
COPY services/delivery-service /app/services/delivery-service
COPY services/auth-service/pom.xml /app/services/auth-service/pom.xml
COPY services/order-service/pom.xml /app/services/order-service/pom.xml
COPY services/restaurant-service/pom.xml /app/services/restaurant-service/pom.xml
COPY services/api-gateway/pom.xml /app/services/api-gateway/pom.xml

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -pl services/delivery-service -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/services/delivery-service/target/delivery-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
