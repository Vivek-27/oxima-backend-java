# Use a base image with Java
FROM openjdk:17-jdk-slim

# Set working directory inside container
WORKDIR /app

# Copy jar file
COPY target/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
