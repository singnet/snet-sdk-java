package com.example.snetdemo;


import android.content.Context;
import android.content.res.Resources;

import java.io.Closeable;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.StaticConfiguration;
import io.singularitynet.sdk.client.Sdk;

public class SnetSdk implements Closeable
{
    private final Sdk sdk;

    public SnetSdk(Context context)
    {
        Resources res = context.getResources();
        // See README.md on how to set channel signer private key via resources
        String privateKey = res.getString(R.string.channel_key);
        String infuraID = res.getString(R.string.infura_id);
        Configuration config = StaticConfiguration.newBuilder()
            .setEthereumJsonRpcEndpoint("https://mainnet.infura.io/v3/" + infuraID)
            .setIpfsEndpoint("http://ipfs.singularitynet.io:80")
            .setSignerType(Configuration.SignerType.PRIVATE_KEY)
            .setSignerPrivateKey(Utils.hexToBytes(privateKey))
            .build();
        sdk = new Sdk(config);

    }

    public Sdk getSdk() {
        return sdk;
    }

    @Override
    public void close() {
        sdk.shutdown();
    }

}
