## Example of Android studio project that uses snet-sdk-java

Android demo apps presented here use snet-sdk-java to call SingularityNET services and display received results. 


There are two services integrated at the moment:

* [Semantic Segmentation](https://beta.singularitynet.io/servicedetails/org/snet/service/semantic-segmentation)

* [Style Transfer](https://beta.singularitynet.io/servicedetails/org/snet/service/style-transfer)

To run a compiled app on Ethereum main network you should have a channel with appropriate AGI balance. 
A channel can be created with [snet-cli](https://github.com/singnet/snet-cli).

In order to compile the SNetDemo app you should provide a channel ID and a 32 byte long private key written as 64 characters hex number. 
We use Infura API suite to get access over HTTPS to the Ethereum network, so you should provide Infura ID for your project as well.
Go to SNetDemo/app/src/main/res/values/ directory and create the following channel_key.xml file:

```
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <integer
        name="channel_id"> CHANNEL_ID
    </integer>


    <string
        name="channel_key">CHANNEL_PRIVATE_KEY
    </string>
    
    <string
        name="infura_id">INFURA_PROJECT_ID
    </string>

</resources>
```

For example

```
<integer
    name="channel_id"> 12
</integer>

<string
    name="channel_key">000102030405060708090A0B0C0D0E0F102132435465768798A9BACBDCEDFE0F
</string>

<string name="infura_id"> 4lmypoyrubrw0x1pltvintxy6wrodgg7
</string>

```

(*The values in the example are not valid, they are just randomly generated*)