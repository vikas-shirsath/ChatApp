# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw.cmd mvnw pom.xml ./
# Fix line endings for mvnw if needed
RUN if [ -f mvnw ]; then chmod +x mvnw; fi
# Download dependencies first (cached layer)
RUN ./mvnw dependency:go-offline -B 2>/dev/null || true
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B 2>/dev/null || \
    (chmod +x mvnw.cmd && ./mvnw.cmd clean package -DskipTests -B)

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/chatapp-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
