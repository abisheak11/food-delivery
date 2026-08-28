FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml /app/
COPY services/order-service /app/services/order-service
COPY services/auth-service/pom.xml /app/services/auth-service/pom.xml
COPY services/restaurant-service/pom.xml /app/services/restaurant-service/pom.xml
COPY services/api-gateway/pom.xml /app/services/api-gateway/pom.xml

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -pl services/order-service -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/services/order-service/target/order-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
