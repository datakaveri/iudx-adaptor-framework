FROM maven:latest as deps
RUN git clone https://github.com/rraks/bahir-flink.git \
    && cd bahir-flink \
    && mvn -DskipTests clean install


FROM flink:1.15.1

# install python3: it has updated Python to 3.9 in Debian 11 and so install Python 3.7 from source
# it currently only supports Python 3.6, 3.7 and 3.8 in PyFlink officially.
RUN apt clean && apt update -y && \
apt install -y build-essential libssl-dev zlib1g-dev libbz2-dev libffi-dev && \
wget https://www.python.org/ftp/python/3.7.9/Python-3.7.9.tgz && \
tar -xvf Python-3.7.9.tgz && \
cd Python-3.7.9 && \
./configure --without-tests --enable-shared && \
make -j6 && \
make install && \
ldconfig /usr/local/lib && \
cd .. && rm -f Python-3.7.9.tgz && rm -rf Python-3.7.9 && \
ln -s /usr/local/bin/python3 /usr/local/bin/python && \
apt clean && \
rm -rf /var/lib/apt/lists/*

RUN mkdir /opt/flink/plugins/s3 \
    && curl https://repo1.maven.org/maven2/org/apache/flink/flink-s3-fs-presto/1.15.1/flink-s3-fs-presto-1.15.1.jar --output /opt/flink/plugins/s3/flink-s3-fs-presto-1.15.1.jar

RUN mkdir /opt/flink/plugins/rmq \
    && curl https://repo1.maven.org/maven2/org/apache/flink/flink-connector-rabbitmq/1.15.1/flink-connector-rabbitmq-1.15.1.jar --output /opt/flink/plugins/rmq/flink-connector-rabbitmq-1.15.1.jar

RUN mkdir /opt/flink/plugins/fasterxml \
    && curl https://repo1.maven.org/maven2/com/fasterxml/uuid/java-uuid-generator/4.0/java-uuid-generator-4.0.jar --output /opt/flink/plugins/fasterxml/java-uuid-generator-4.0.jar

RUN mkdir /opt/flink/plugins/json \
    && curl https://repo1.maven.org/maven2/org/json/json/2020518/json-2020518.jar --output /opt/flink/plugins/json/json-2020518.jar 

RUN mkdir /opt/flink/plugins/http \
    && curl https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.2/httpclient-4.5.2.jar --output /opt/flink/plugins/http/httpclient-4.5.2.jar

