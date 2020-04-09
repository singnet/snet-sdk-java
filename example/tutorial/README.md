# Overview

Java SDK tutorial application built using Maven. See the [tutorial
page](https://dev.singularitynet.io/tutorials/client/java).

Run local environment:
```sh
docker run -p 5002:5002 -p 8545:8545 -p 7000:7000 \
    -ti singularitynet/snet-local-env:3.0.0
```

Build and run application:
```sh
mvn package exec:java
```
