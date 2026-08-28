FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml /app/
COPY services/restaurant-service /app/services/restaurant-service
COPY services/auth-service/pom.xml /app/services/auth-service/pom.xml
COPY services/order-service/pom.xml /app/services/order-service/pom.xml
COPY services/api-gateway/pom.xml /app/services/api-gateway/pom.xml

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -pl services/restaurant-service -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/services/restaurant-service/target/restaurant-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
