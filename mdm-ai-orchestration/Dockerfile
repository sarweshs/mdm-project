# Use a slim OpenJDK image for Java 17
FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y curl

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the Maven build output
# Use the repackaged Spring Boot executable JAR only
ARG JAR_FILE=target/mdm-ai-orchestration-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Expose the port the application runs on
EXPOSE 8083

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]