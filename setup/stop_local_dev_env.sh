#!/bin/bash

# To be executed from project root

./setup/scripts/stop_monitoring_service.sh
docker-compose -f ./setup/flink/docker-compose.yml down -v
docker-compose -f ./setup/rmq/docker-compose.yml down -v
docker-compose -f ./setup/postgres/docker-compose.yml down -v
docker-compose -f ./setup/mockserver/docker-compose.yml down -v
docker-compose -f ./setup/server/docker-compose.yml down -v
docker-compose -f ./setup/flink/docker-compose.yml down -v
docker network rm adaptor-net
