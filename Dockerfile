# ==========================================
# Stage 1: Build & Package
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21-jammy AS builder

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Runtime Environment
# ==========================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create dedicated non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Create keys directory with appropriate permissions
RUN mkdir -p /app/keys && chown -R appuser:appgroup /app

# Copy the built application JAR
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE="default"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
