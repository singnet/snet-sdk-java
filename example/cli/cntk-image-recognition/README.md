# CNTK image recognition service client

This command line application demonstrates calling [AI Sight](https://beta.singularitynet.io/servicedetails/org/snet/service/cntk-image-recon)
SingularityNet service.

To run example one need passing payment channel signer private key as command
line parameter:
```sh
mvn package exec:java \
    -Dexec.mainClass="io.singularitynet.sdk.example.CntkImageRecognition" \
    -Dexec.args="<channel-signer-private-key>"
```
