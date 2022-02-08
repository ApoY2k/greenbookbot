FROM openjdk:17-slim as final

WORKDIR /app

COPY target/greenbookbot-1.0.0-jar-with-dependencies.jar .

CMD ["java", "-jar", "greenbookbot-1.0.0-jar-with-dependencies.jar"]
