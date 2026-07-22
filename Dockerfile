# ==============================
# ETAPA 1: COMPILACIÓN
# ==============================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -ntp clean package


# ==============================
# ETAPA 2: EJECUCIÓN
# ==============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build \
    /workspace/target/*-boot.jar \
    app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]