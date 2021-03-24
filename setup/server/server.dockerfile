# Run from project root directory

ARG VERSION="1"

FROM maven:latest as deps
WORKDIR /usr/share/app/iudx-adaptor-framework
RUN mkdir adaptor \
    && mkdir template
COPY ./pom.xml ./pom.xml
COPY ./adaptor/pom.xml ./adaptor/pom.xml
COPY ./template/pom.xml ./template/pom.xml
RUN cd adaptor \
    && mvn clean package 


FROM deps as builder
COPY ./adaptor ./adaptor/
COPY ./template ./adaptor/template
RUN cd adaptor \
    && mvn clean package -Dmaven.test.skip=true


FROM builder
ENV JAR="server.jar"
WORKDIR /usr/share/app/iudx-adaptor-framework/
COPY --from=builder /usr/share/app/iudx-adaptor-framework/adaptor/target/${JAR} ./${JAR}
