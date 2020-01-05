## Example of Android studio project that uses snet-sdk-java

Android demo apps presented here use snet-sdk-java to call SingularityNET services and display received results. 


There are two services integrated at the moment:

* [Semantic Segmentation](https://beta.singularitynet.io/servicedetails/org/snet/service/semantic-segmentation)

* [Style Transfer](https://beta.singularitynet.io/servicedetails/org/snet/service/style-transfer)

To run a compiled app on Ethereum main network you should have a channel with appropriate AGI balance. 
A channel can be created with [snet-cli](https://github.com/singnet/snet-cli).

In order to compile the SNetDemo app you should provide a channel ID and a private key. Go to SNetDemo/app/src/main/res/values/ directory and create the following channel_key.xml file:

```
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <integer
        name="channel_id"> YOUR_CHANNEL_ID
    </integer>


    <string
        name="channel_key">YOUR_PRIVATE_KEY
    </string>

</resources>
```
