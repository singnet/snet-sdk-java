## Example of Android studio project that uses snet-sdk-java

Android demo apps presented here use snet-sdk-java to call SingularityNET
services and display received results. 

There are two services integrated at the moment:

* [Semantic Segmentation](https://beta.singularitynet.io/servicedetails/org/snet/service/semantic-segmentation)
* [Style Transfer](https://beta.singularitynet.io/servicedetails/org/snet/service/style-transfer)

To run a compiled app on Ethereum main network you should have some amount of
ETH and AGI tokens at your Ethereum address.

In order to build and run the SNetDemo app you should provide:
- an Ethereum RPC endpoint URL;
- 32 byte long private key written as 64 characters hex number.

The simplest way to get Ethereum RPC endpoint is registering on
[Infura](https://infura.io), get your personal project ID and use the following
URL: `https://mainnet.infura.io/v3/<project-id>`.

Please put settings above into [ethereum.properties](./ethereum.propereties)
file in the root project folder using format below:
```
# Put Ethereum JSON RPC endpoint below
ethereum.json.rpc.endpoint=https://mainnet.infura.io/v3/e7732e1f679e461b9bb4da5653ac3fc2
# Put Ethereum identity private key in hex below
identity.private.key.hex=000102030405060708090A0B0C0D0E0F102132435465768798A9BACBDCEDFE0F
ipfs.endpoint=http://ipfs.singularitynet.io:80
identity.type=PRIVATE_KEY
gas.price=6100000000
gas.limit=200000
```
