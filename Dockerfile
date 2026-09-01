# ===========================================================
# Stage 1: Build Stage (Maven + Eclipse Temurin JDK 21)
# ===========================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy dependency definition first for layer caching
COPY pom.xml .

# Download dependencies offline
RUN mvn dependency:go-offline -B

# Copy project source code
COPY src ./src

# Build production jar without running unit tests
RUN mvn clean package -DskipTests

# ===========================================================
# Stage 2: Runtime Stage (Lightweight JRE 21 + Security)
# ===========================================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create a non-root system user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create directory for JWT public keys & set ownership
RUN mkdir -p /app/keys && chown -R appuser:appgroup /app

# Copy the built jar from the builder stage
COPY --from=builder /build/target/*.jar /app/app.jar

# Set file permissions for non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose the service port
EXPOSE 8082

# JVM flags for container memory management
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
