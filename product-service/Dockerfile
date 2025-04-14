FROM ubuntu:24.04

RUN apt-get update && \
    apt-get install -y openjdk-21-jre && \
    apt-get clean

WORKDIR /app
COPY build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]