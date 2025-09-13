# Use multi-stage build for smaller image size
FROM openjdk:21-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM openjdk:21-jre-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user for security
RUN addgroup --system nexus && adduser --system nexus --ingroup nexus

# Set working directory
WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /app/target/Nexus-*.jar app.jar

# Create uploads directory
RUN mkdir -p uploads && chown -R nexus:nexus /app

# Switch to non-root user
USER nexus

# Expose port
EXPOSE 8080 9092

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]