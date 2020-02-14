# Example service client

This command line application demonstrates calling [Example Service](https://beta.singularitynet.io/servicedetails/org/snet/service/example-service)
SingularityNet service.

To run example one need passing payment channel signer private key as command
line parameter:
```sh
mvn package exec:java \
    -Dexec.mainClass="io.singularitynet.sdk.example.ExampleService" \
    -Dexec.args="<channel-signer-private-key>"
```
