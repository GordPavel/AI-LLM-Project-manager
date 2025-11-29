# ========== ЭТАП 1: Сборка JAR ==========
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Кэшируем зависимости (ускоряет пересборку)
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ========== ЭТАП 2: Запуск ==========
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем готовый JAR из предыдущего этапа
COPY --from=builder /app/target/ai-helper-*.jar app.jar

# Открываем порт
EXPOSE 8080

# Запускаем
ENTRYPOINT ["java", "-jar", "app.jar"]