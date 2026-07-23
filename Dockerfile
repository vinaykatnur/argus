# Stage 1: Build Frontend
FROM node:18-alpine AS frontend-builder
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Backend
FROM maven:3.9.6-eclipse-temurin-21 AS backend-builder
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src/ ./src/
# Copy frontend build output to Spring Boot static resources
COPY --from=frontend-builder /frontend/dist/ ./src/main/resources/static/
RUN mvn clean package -DskipTests

# Stage 3: Runner
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-builder /app/target/argus-*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
