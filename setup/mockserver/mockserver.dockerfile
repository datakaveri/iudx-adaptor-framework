# Run from project root directory

ARG VERSION="1"

FROM maven:latest as deps
WORKDIR /usr/share/app/iudx-adaptor-framework
RUN mkdir mockserver
COPY ./mockserver/ ./mockserver
RUN cd mockserver && mvn clean package

FROM deps as builder
RUN cd mockserver && mvn clean package
ENTRYPOINT java -jar mockserver/target/mockserver.jar
