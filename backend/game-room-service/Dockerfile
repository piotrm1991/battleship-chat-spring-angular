FROM openjdk:17-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=game-room-service-0.0.1-SNAPSHOT.jar
COPY ./target/${JAR_FILE} app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=docker", "-jar", "/app.jar"]