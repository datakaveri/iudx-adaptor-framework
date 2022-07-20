FROM maven:latest as deps

FROM flink:1.15.1

RUN mkdir /opt/flink/plugins/s3 \
    && curl https://repo1.maven.org/maven2/org/apache/flink/flink-s3-fs-presto/1.15.1/flink-s3-fs-presto-1.15.1.jar --output /opt/flink/plugins/s3/flink-s3-fs-presto-1.15.1.jar 

RUN mkdir /opt/flink/plugins/rmq \
    && curl https://repo1.maven.org/maven2/org/apache/flink/flink-connector-rabbitmq/1.15.1/flink-connector-rabbitmq-1.15.1.jar --output /opt/flink/plugins/rmq/flink-connector-rabbitmq-1.15.1.jar 

RUN mkdir /opt/flink/plugins/fasterxml \
    && curl https://repo1.maven.org/maven2/com/fasterxml/uuid/java-uuid-generator/4.0/java-uuid-generator-4.0.jar --output /opt/flink/plugins/fasterxml/java-uuid-generator-4.0.jar

RUN mkdir /opt/flink/plugins/json \
    && curl https://repo1.maven.org/maven2/org/json/json/2020518/json-2020518.jar --output /opt/flink/plugins/json/json-2020518.jar 

RUN mkdir /opt/flink/plugins/http \
    && curl https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar --output /opt/flink/plugins/http/httpclient-4.5.13.jar


