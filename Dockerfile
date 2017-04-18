FROM openjdk:8-jdk

MAINTAINER juliens@microsoft.com


WORKDIR /app

COPY src .

RUN /app/gradlew build -x test


ENTRYPOINT sh run.sh
