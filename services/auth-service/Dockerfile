FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml /app/
COPY services/auth-service /app/services/auth-service
COPY services/order-service/pom.xml /app/services/order-service/pom.xml
COPY services/restaurant-service/pom.xml /app/services/restaurant-service/pom.xml
COPY services/api-gateway/pom.xml /app/services/api-gateway/pom.xml

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -pl services/auth-service -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/services/auth-service/target/auth-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
