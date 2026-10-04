FROM maven:3.9.16-eclipse-temurin-25 AS builder
# Maven build environment with Java 21
WORKDIR /app
# Set the working directory inside the container
COPY pom.xml .
# Copy pom.xml first so Docker can cache dependency downloads
RUN mvn dependency:go-offline -B
# Download Maven dependencies and plugins ahead of the build.
# Keeping this layer separate allows Docker to reuse the cached
# dependencies when only source code changes.

COPY src ./src
# Copy application source code into the container

RUN mvn clean package
# Build the Spring Boot application and generate the JAR in target/

FROM eclipse-temurin:25 AS runner

WORKDIR /app

COPY --from=builder ./app/target/billing-service-0.0.1-SNAPSHOT.jar app.jar
# Copy the generated JAR from the builder stage into the runtime image

EXPOSE 4001
EXPOSE 9001

ENTRYPOINT ["java","-jar","app.jar"]