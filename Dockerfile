FROM openjdk:17-slim as final

WORKDIR /app

COPY target/greenbookbot-1.0-SNAPSHOT.jar .

CMD ["java", "-jar", "greenbookbot-1.0-SNAPSHOT.jar"]
