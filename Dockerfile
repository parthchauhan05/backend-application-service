# Multi-stage build for Spring Boot
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build JAR (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE ${PORT:8080}

# Start app
ENTRYPOINT ["java", "-jar", "app.jar"]