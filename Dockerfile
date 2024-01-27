# Docker 镜像构建
# @author 金蝉
FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/target/FitBuddy-backend-1.0.0-RELEASE.jar","--spring.profiles.active=prod"]
