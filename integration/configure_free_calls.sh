#!/bin/sh

start_environment.sh

cd ${ROOT}/example-service

snet identity deployer
snet service metadata-set-free-calls default_group 2
snet service update-metadata example-org example-service -y
