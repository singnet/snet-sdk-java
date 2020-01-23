#!/bin/sh
DIR=$(dirname $0)

docker build -t singularitynet/java-sdk-integration-test-env ${DIR}
