FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY services/payment-service/pom.xml services/payment-service/
RUN mvn dependency:go-offline -B -f services/payment-service/pom.xml

COPY services/payment-service/src services/payment-service/src
RUN mvn clean package -DskipTests -f services/payment-service/pom.xml

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/services/payment-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
