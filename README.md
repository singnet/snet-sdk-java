# SingularityNet Java SDK

[![CircleCI](https://circleci.com/gh/singnet/snet-sdk-java.svg?style=svg)](https://circleci.com/gh/singnet/snet-sdk-java)
[![codecov](https://codecov.io/gh/singnet/snet-sdk-java/branch/master/graph/badge.svg)](https://codecov.io/gh/singnet/snet-sdk-java)

## Class diagram

![Class diagram](./docs/class-diagram.svg)
[Source code](./docs/class-diagram.plantuml)

## How to build

Integration testing is disabled by default. To run full build including
integration tests use:
```
mvn install -DskipITs=false -P run-integration-environment
```

The command about automatically starts integration environment docker before
running tests and stops after it. To start integration environment manually
execute:
```
docker run -d \
    --name java-sdk-integration-environment \
    -p 5002:5002 -p 8545:8545 -p 7000:7000 \
    singularitynet/java-sdk-integration-test-env:2.0.2
```
Then you can run build with integration testing using:
```
mvn install -DskipITs=false
```

Running integration tests is a time consuming process so to make fast build
running unit tests only use:
```
mvn install
```
