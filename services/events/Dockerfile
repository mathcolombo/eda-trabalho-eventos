# Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]