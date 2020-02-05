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
mvn install -DskipITs=false
```

Running integration tests is a time consuming process so to make fast build
running unit tests only use:
```
mvn install
```
