#!/bin/bash

# To be executed from project root

# Mock http server
#docker build -t datakaveri/adaptor-mockserver:2.3.3 -f setup/mockserver/mockserver.dockerfile .
docker build -t datakaveri/adaptor-server:2.3.3 -f setup/server/server.dockerfile .


#cd ./setup/flink/
#docker-compose build
