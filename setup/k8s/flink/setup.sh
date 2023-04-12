#!/bin/bash

FLINK_VERSION=1.16.1
SCALA_VERSION=2.12

wget https://dlcdn.apache.org/flink/flink-${FLINK_VERSION}/flink-${FLINK_VERSION}-bin-scala_${SCALA_VERSION}.tgz -O flink.tgz


tar -xf flink.tgz -C .
mv flink-${FLINK_VERSION}/* .
cp flink-conf.yaml conf/
cp log4j-console.properties conf/
rm -rf flink.tgz
rm -rf flink-${FLINK_VERSION}