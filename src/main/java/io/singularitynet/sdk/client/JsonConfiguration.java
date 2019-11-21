package io.singularitynet.sdk.client;

import java.net.URL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;
import com.google.common.base.Preconditions;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Signer;
import io.singularitynet.sdk.ethereum.MnemonicIdentity;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;

public class JsonConfiguration implements Configuration {

    private final String ethereumJsonRpcEndpoint;
    private final URL ipfsUrl;
    private final String signerType;
    private final String signerMnemonic;
    private final String signerPrivateKeyBase64;

    public JsonConfiguration(String json) {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        JsonConfiguration config = gson.fromJson(json, JsonConfiguration.class);
        Preconditions.checkArgument(config.ethereumJsonRpcEndpoint != null,
                "Field ethereum_json_rpc_endpoint is required");
        this.ethereumJsonRpcEndpoint = config.ethereumJsonRpcEndpoint;
        this.ipfsUrl = config.ipfsUrl;
        this.signerType = config.signerType;
        this.signerMnemonic = config.signerMnemonic;
        this.signerPrivateKeyBase64 = config.signerPrivateKeyBase64;
    }

    @Override
    public String getEthereumJsonRpcEndpoint() {
        return ethereumJsonRpcEndpoint;
    }

    @Override
    public URL getIpfsUrl() {
        return ipfsUrl;
    }

    @Override
    public SignerType getSignerType() {
        return Enum.valueOf(SignerType.class, signerType.toUpperCase());
    }

    @Override
    public String getSignerMnemonic() {
        return signerMnemonic;
    }

    @Override
    public byte[] getSignerPrivateKey() {
        return Utils.base64ToBytes(signerPrivateKeyBase64);
    }

}
