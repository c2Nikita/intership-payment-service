FROM eclipse-temurin:21-jdk
WORKDIR /app
RUN useradd -ms /bin/bash appuser
COPY build/libs/*.jar app.jar
USER appuser
ENTRYPOINT ["java","-Dspring.profiles.active=docker", "-jar", "app.jar"]