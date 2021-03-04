#!/bin/bash

# To be executed from project root

# Mock http server
docker build -t iudx/adaptor-mockserver:latest -f setup/mockserver/mockserver.dockerfile .
docker build -t iudx/adaptor-server:latest -f setup/server/server.dockerfile .
