## Example of Android studio project that uses snet-sdk-java

Android demo apps presented here use snet-sdk-java to call SingularityNET
services and display received results. 


There are two services integrated at the moment:

* [Semantic Segmentation](https://beta.singularitynet.io/servicedetails/org/snet/service/semantic-segmentation)

* [Style Transfer](https://beta.singularitynet.io/servicedetails/org/snet/service/style-transfer)

To run a compiled app on Ethereum main network you should have a channel with
appropriate AGI balance.  A channel can be created with
[snet-cli](https://github.com/singnet/snet-cli).

In order to build and run the SNetDemo app you should provide an Ethereum RPC
endpoint URL and a 32 byte long private key written as 64 characters hex
number. The simplest way to get Ethereum RPC endpoint is registering on
[Infura](https://infura.io), get your personal project ID and use the following
URL: `https://mainnet.infura.io/v3/<project-id>`. Please put settings above
into [ethereum.properties](./ethereum.propereties) file.
