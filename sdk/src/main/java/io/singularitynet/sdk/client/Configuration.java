package io.singularitynet.sdk.client;

import java.net.URL;

import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Signer;

public interface Configuration {

    static enum SignerType {
        MNEMONIC,
        PRIVATE_KEY
    }

    String getEthereumJsonRpcEndpoint();
    URL getIpfsUrl();
    SignerType getSignerType();
    String getSignerMnemonic();
    byte[] getSignerPrivateKey();
    Address getRegistryAddress();
    Address getMultiPartyEscrowAddress();

}
