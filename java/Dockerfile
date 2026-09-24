# --- Stage 1: Build the Maven application ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy the pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and compile the jar file
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Create the runtime environment ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add a non-root system user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the compiled jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot's default port
EXPOSE 8080

# Run the application with optimized container memory flags
ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "app.jar"]
