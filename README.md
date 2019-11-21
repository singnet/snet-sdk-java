# SingularityNet Java SDK

[![CircleCI](https://circleci.com/gh/singnet/snet-sdk-java.svg?style=svg)](https://circleci.com/gh/singnet/snet-sdk-java)
[![codecov](https://codecov.io/gh/singnet/snet-sdk-java/branch/master/graph/badge.svg)](https://codecov.io/gh/singnet/snet-sdk-java)

## Class diagram

![Class diagram](./docs/class-diagram.svg)
[Source code](./docs/class-diagram.plantuml)

## How to build

Install dependencies for the first time:
```
curl -L https://get.web3j.io | bash
source $HOME/.web3j/source.sh
./get_dependencies.sh
```

Build and test:
```
mvn test
```

