# ---------- STAGE 1: BUILD ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy maven wrapper + pom trước để tận dụng cache layer
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Copy source và build
COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ---------- STAGE 2: RUNTIME ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Tạo user non-root (bảo mật, Render khuyến nghị)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

# Render cấp PORT động → đọc từ biến môi trường, fallback 7000
ENV SERVER_PORT=7000
EXPOSE 7000

# Dùng shell form để expand $PORT nếu cần
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-7000} -jar /app/app.jar"]