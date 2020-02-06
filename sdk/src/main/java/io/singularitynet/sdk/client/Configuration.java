package io.singularitynet.sdk.client;

import java.net.URL;
import java.math.BigInteger;
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
    IdentityType getIdentityType();
    Optional<String> getIdentityMnemonic();
    Optional<byte[]> getIdentityPrivateKey();
    Optional<Address> getRegistryAddress();
    Optional<Address> getMultiPartyEscrowAddress();
    Optional<BigInteger> getGasPrice();
    Optional<BigInteger> getGasLimit();

}
