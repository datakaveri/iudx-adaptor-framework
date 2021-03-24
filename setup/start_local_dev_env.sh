#!/bin/bash

# To be executed from project root

docker network create adaptor-net
./setup/scripts/start_monitoring_service.sh
docker-compose -f ./setup/rmq/docker-compose.yml up -d
docker-compose -f ./setup/postgres/docker-compose.yml up -d
docker-compose -f ./setup/mockserver/docker-compose.yml up -d
docker-compose -f ./setup/server/docker-compose.yml up -d
docker-compose -f ./setup/flink/docker-compose.yml up -d
