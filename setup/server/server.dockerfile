# Run from project root directory

ARG VERSION="1"

FROM maven:latest as dependencies

WORKDIR /usr/share/app
COPY pom.xml .
RUN mvn clean package -Dmaven.test.skip=true


FROM dependencies as builder

WORKDIR /usr/share/app
COPY ./src ./src
RUN mvn clean package -Dmaven.test.skip=true



FROM openjdk:14-slim-buster
ENV JAR="server.jar"
WORKDIR /usr/share/app
COPY --from=builder /usr/share/app/target/${JAR} ./${JAR}
