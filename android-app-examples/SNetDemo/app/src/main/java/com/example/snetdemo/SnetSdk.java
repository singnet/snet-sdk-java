package com.example.snetdemo;


import android.content.Context;
import android.content.res.Resources;

import java.io.Closeable;

import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.JsonConfiguration;
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
        String json = "{" +
                "\"ethereum_json_rpc_endpoint\": \"https://mainnet.infura.io/v3/" + infuraID + "\", " +
                "\"ipfs_url\": \"http://ipfs.singularitynet.io:80\"," +
                "\"signer_type\": \"PRIVATE_KEY\"," +
                "\"signer_private_key_hex\": \"" + privateKey + "\"" +
                "}";
        Configuration config = new JsonConfiguration(json);
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
