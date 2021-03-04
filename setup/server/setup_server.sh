#!/bin/bash

# To be executed from project root
docker build -t iudx/adaptor-server:latest -f ./setup/server/server.dockerfile .
