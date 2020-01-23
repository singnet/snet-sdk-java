#!/bin/sh
DIR=$(dirname $0)

. ${DIR}/tag
docker build --build-arg TAG=${TAG} -t singularitynet/java-sdk-integration-test-env:${TAG} ${DIR}
