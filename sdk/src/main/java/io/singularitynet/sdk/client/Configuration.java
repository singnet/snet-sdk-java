package io.singularitynet.sdk.client;

import java.net.URL;
import java.util.Optional;

import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Identity;

public interface Configuration {

    static enum IdentityType {
        MNEMONIC,
        PRIVATE_KEY
    }

    URL getEthereumJsonRpcEndpoint();
    URL getIpfsEndpoint();
    IdentityType getSignerType();
    Optional<String> getSignerMnemonic();
    Optional<byte[]> getSignerPrivateKey();
    Optional<Address> getRegistryAddress();
    Optional<Address> getMultiPartyEscrowAddress();

}
