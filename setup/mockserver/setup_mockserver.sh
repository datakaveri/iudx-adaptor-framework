#!/bin/bash

# To be executed from project root
docker build -t iudx/adaptor-mockserver:latest -f setup/mockserver/mockserver.dockerfile .
