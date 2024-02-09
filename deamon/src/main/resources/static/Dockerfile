# Usa l'immagine di Maven ufficiale come base
FROM maven:3.8.4-jdk-11 AS builder

# Imposta il working directory nel container
WORKDIR /app

# Copia il file .zip generato da Swagger Codegen nel container
COPY server.zip .

# Estrai il progetto dal file .zip
RUN unzip server.zip -d project

# Imposta il working directory nel progetto estratto
WORKDIR /app/project/generatedServer

# Esegui il build del progetto con Maven
RUN mvn clean install -DskipTests

# Usa un'immagine di OpenJDK come base per l'esecuzione del progetto
FROM openjdk:11-jre-slim

# Copia il file JAR generato durante il build nel container
COPY --from=builder /app/project/generatedServer/target/swagger-spring-1.0.0.jar /app/swagger-spring-1.0.0.jar

# Esponi la porta su cui il server Spring Boot ascolterà le richieste HTTP
EXPOSE 8080

# Avvia l'applicazione Spring Boot quando il container viene avviato
CMD ["java", "-jar", "/app/swagger-spring-1.0.0.jar"]
