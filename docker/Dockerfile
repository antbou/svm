FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

RUN mkdir -p /app/screenshots

COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .
RUN mvn clean package -DskipTests

FROM mcr.microsoft.com/playwright/java:v1.50.0-noble

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app.jar"]
