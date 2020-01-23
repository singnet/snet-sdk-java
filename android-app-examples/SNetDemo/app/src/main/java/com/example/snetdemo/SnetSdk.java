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
//                "\"ethereum_json_rpc_endpoint\": \"https://mainnet.infura.io\", " +
                "\"ipfs_url\": \"http://ipfs.singularitynet.io:80\"," +
                "\"signer_type\": \"PRIVATE_KEY\"," +
                "\"signer_private_key_base64\": \"" + hexToBase64(privateKey) + "\"" +
                "}";
        Configuration config = new JsonConfiguration(json);
        sdk = new Sdk(config);

    }

    private static String hexToBase64(String hex) {
        return io.singularitynet.sdk.common.Utils.bytesToBase64(
                io.singularitynet.sdk.common.Utils.hexToBytes(hex));
    }

    public Sdk getSdk() {
        return sdk;
    }

    @Override
    public void close() {
        sdk.shutdown();
    }

}
