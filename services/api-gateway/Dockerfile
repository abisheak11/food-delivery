FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml /app/
COPY services/api-gateway /app/services/api-gateway
COPY services/auth-service/pom.xml /app/services/auth-service/pom.xml
COPY services/order-service/pom.xml /app/services/order-service/pom.xml
COPY services/restaurant-service/pom.xml /app/services/restaurant-service/pom.xml

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -pl services/api-gateway -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/services/api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
