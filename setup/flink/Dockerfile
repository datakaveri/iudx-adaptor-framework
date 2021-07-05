FROM flink:1.13.1-scala_2.12-java11

RUN mkdir /opt/flink/plugins/s3 \
    && curl https://repo1.maven.org/maven2/org/apache/flink/flink-s3-fs-presto/1.13.1/flink-s3-fs-presto-1.13.1.jar --output /opt/flink/plugins/s3/flink-s3-fs-presto-1.13.1.jar 
