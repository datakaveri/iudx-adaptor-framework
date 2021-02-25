#!/bin/sh

FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager"
docker network create flink-network

docker run \
    -d \
    --rm \
    --name=jobmanager \
    --network flink-network \
    --publish 8081:8081 \
    --env FLINK_PROPERTIES="${FLINK_PROPERTIES}" \
    flink:1.11.2-scala_2.12-java8 jobmanager
    


docker run \
    -d \
    --rm \
    --name=taskmanager \
    --network flink-network \
    --env FLINK_PROPERTIES="${FLINK_PROPERTIES}" \
    flink:1.11.2-scala_2.12-java8 taskmanager
