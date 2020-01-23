# SingularityNet Java SDK

[![CircleCI](https://circleci.com/gh/singnet/snet-sdk-java.svg?style=svg)](https://circleci.com/gh/singnet/snet-sdk-java)
[![codecov](https://codecov.io/gh/singnet/snet-sdk-java/branch/master/graph/badge.svg)](https://codecov.io/gh/singnet/snet-sdk-java)

## Class diagram

![Class diagram](./docs/class-diagram.svg)
[Source code](./docs/class-diagram.plantuml)

## How to build

Integration testing is enabled by default. But running integration tests is a
time consuming process so to make fast build running unit tests only use:
```
mvn install -DskipITs
```

In order to build and run integration test the environment docker should be
built first:
```
./integration/build.sh
```

Then run the full build using:
```
mvn install
```

