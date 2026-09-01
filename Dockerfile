# ===========================================================
# Stage 1: Build Stage (Maven + Eclipse Temurin JDK 21)
# ===========================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy dependency descriptor first for layer caching
COPY pom.xml .

# Pre-fetch dependencies offline
RUN mvn dependency:go-offline -B

# Copy application source code
COPY src ./src

# Build production JAR without running unit tests
RUN mvn clean package -DskipTests

# ===========================================================
# Stage 2: Runtime Stage (Lightweight JRE 21 + Security)
# ===========================================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create non-root system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create target directory for runtime key mounting
RUN mkdir -p /app/keys && chown -R appuser:appgroup /app

# Copy compiled JAR from builder stage
COPY --from=builder /build/target/*.jar /app/app.jar

# Set file permissions for non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose microservice port
EXPOSE 8082

# JVM configuration for container resource limits
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run Spring Boot microservice
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
