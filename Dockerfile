# ---------- STAGE 1: BUILD ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper và file cấu hình
COPY .mvn ./.mvn
COPY mvnw mvnw.cmd ./
COPY pom.xml ./

# Copy source code
COPY src ./src

# Cấp quyền thực thi cho wrapper (cần trên Linux/macOS)
RUN chmod +x mvnw

# Build ứng dụng (bỏ qua test để nhanh)
RUN ./mvnw clean package -DskipTests

# ---------- STAGE 2: RUNTIME ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file JAR từ stage build
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 7000 (khớp với server.port)
EXPOSE 7000

# Lệnh khởi chạy
ENTRYPOINT ["java", "-jar", "/app/app.jar"]