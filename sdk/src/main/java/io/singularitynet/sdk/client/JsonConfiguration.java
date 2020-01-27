package io.singularitynet.sdk.client;

import java.net.URL;
import java.util.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;

public class JsonConfiguration implements Configuration {

    private final String ethereumJsonRpcEndpoint;
    private final URL ipfsUrl;
    private final String signerType;
    private final String signerMnemonic;
    private final String signerPrivateKeyBase64;
    private final String registryAddress;
    private final String multiPartyEscrowAddress;

    public JsonConfiguration(String json) {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        JsonConfiguration config = gson.fromJson(json, JsonConfiguration.class);
        this.ethereumJsonRpcEndpoint = config.ethereumJsonRpcEndpoint;
        this.ipfsUrl = config.ipfsUrl;
        this.signerType = config.signerType;
        this.signerMnemonic = config.signerMnemonic;
        this.signerPrivateKeyBase64 = config.signerPrivateKeyBase64;
        this.registryAddress = config.registryAddress;
        this.multiPartyEscrowAddress = config.multiPartyEscrowAddress;
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

    @Override
    public Optional<Address> getRegistryAddress() {
        if (registryAddress == null) {
            return Optional.<Address>empty();
        }
        return Optional.of(new Address(registryAddress));
    }

    @Override
    public Optional<Address> getMultiPartyEscrowAddress() {
        if (multiPartyEscrowAddress == null) {
            return Optional.<Address>empty();
        }
        return Optional.of(new Address(multiPartyEscrowAddress));
    }

}
