# ===============================
# 1️⃣ BUILD STAGE
# ===============================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

#Copiamos primero pom para cachear dependencias
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN mvn dependency:go-offline -B

#Copiamos el codigo
COPY src src

#Compilamos el JAR
RUN mvn clean package -DskipTests

# ===============================
# 2️⃣ RUNTIME STAGE
# ===============================
FROM eclipse-temurin:17-jre

# Copiamos solo el JAR final
COPY --from=build /app/target/*.jar app.jar

# Puerto interno del contenedor
EXPOSE 8080

# Arranque
ENTRYPOINT ["java", "-jar", "app.jar"]