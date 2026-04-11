# Multi-stage Spring Boot for Render
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build JAR
RUN mvn clean package -DskipTests

# Production runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose 8080 (Render maps to external PORT)
EXPOSE 8080

# Run with Render's PORT env var
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]