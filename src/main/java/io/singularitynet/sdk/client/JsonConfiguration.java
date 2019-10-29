package io.singularitynet.sdk.client;

import java.net.URL;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import io.ipfs.api.IPFS;
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
    private final SignerType signerType;
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
    public Web3j getWeb3j() {
        return Web3j.build(new HttpService(ethereumJsonRpcEndpoint));
    }

    @Override
    public ContractGasProvider getContractGasProvider() {
        return new DefaultGasProvider();
    }

    @Override
    public IPFS getIpfs() {
        // TODO: support https
        return new IPFS(ipfsUrl.getHost(), ipfsUrl.getPort());
    }

    @Override
    public Signer getSigner() {
        switch (signerType) {
            case MNEMONIC:
                return new MnemonicIdentity(signerMnemonic);
            case PRIVATE_KEY:
                return new PrivateKeyIdentity(Utils.base64ToBytes(signerPrivateKeyBase64));
            default:
                throw new IllegalArgumentException("Unexpected signer type: " + signerType);
        }
    }

    private static enum SignerType {
        MNEMONIC,
        PRIVATE_KEY
    }

}
