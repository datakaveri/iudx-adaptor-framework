# Run from project root directory

ARG VERSION="1"

FROM maven:latest as deps
WORKDIR /usr/share/app/iudx-adaptor-framework
RUN mkdir server \
    && mkdir framework \
    && mkdir template
COPY ./server/pom.xml ./server/pom.xml
COPY ./framework/pom.xml ./framework/pom.xml
COPY ./template/pom.xml ./template/pom.xml
RUN cd server && mvn clean package
RUN cd framework && mvn clean package


FROM deps as builder
ENV JAR="server.jar"
COPY ./server ./server/
COPY ./template ./template
COPY ./framework ./framework
RUN cd server \
    && mvn clean package -Dmaven.test.skip=true
RUN mkdir upload-jar \
    && cd framework \
    && mvn clean install -Dmaven.test.skip=true
