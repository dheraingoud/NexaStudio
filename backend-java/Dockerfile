# ═══════════════════════════════════════════════════════════════════
# NexaStudio Backend - Dockerfile
# Multi-stage build for optimized production image
# ═══════════════════════════════════════════════════════════════════

# ─────────────────────────────────────────────────────────────────────
# Stage 1: Build the application
# ─────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw.cmd .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────────────────────────────
# Stage 2: Create minimal runtime image
# ─────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Security: Run as non-root user
RUN addgroup -g 1001 -S nexastudio && \
    adduser -u 1001 -S nexastudio -G nexastudio

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R nexastudio:nexastudio /app

# Switch to non-root user
USER nexastudio

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# ─────────────────────────────────────────────────────────────────────
# Environment Variables (set via Render dashboard, NOT here)
# ─────────────────────────────────────────────────────────────────────
# Required secrets (configure in Render):
#   - DATABASE_URL: PostgreSQL connection string
#   - DATABASE_USER: Database username  
#   - DATABASE_PASSWORD: Database password
#   - JWT_SECRET: Secret key for JWT signing
#   - NVIDIA_API_KEY: NVIDIA NIM API key for AI models
#   - GEMINI_API_KEY: Google Gemini API key (fallback)
#
# Optional configuration:
#   - SPRING_PROFILES_ACTIVE: Active profile (default: production)
#   - SERVER_PORT: Port to run on (default: 8080)

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=production"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
