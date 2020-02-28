package com.example.snetdemo;

import android.content.Context;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

import io.singularitynet.sdk.client.ConfigurationUtils;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.Sdk;

public class SnetSdk implements Closeable
{
    private final Sdk sdk;

    public SnetSdk(Context context) throws IOException
    {
        Properties props = new Properties();
        props.load(context.getAssets().open("ethereum.properties"));
        Configuration config = ConfigurationUtils.fromProperties(props);
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
