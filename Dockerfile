# Use an official OpenJDK runtime as a base image
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/receipt-processor-challenge-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

