package io.singularitynet.sdk.client;

import java.net.URL;
import java.math.BigInteger;
import java.util.Optional;

import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Identity;

/**
 * SDK configuration interface.
 */
public interface Configuration {

    /**
     * Type of the identity.
     */
    static enum IdentityType {
        /**
         * Mnemonic identity.
         */
        MNEMONIC,
        /**
         * Explicit private key identity
         */
        PRIVATE_KEY
    }

    /**
     * Return Ethereum JSON RPC endpoint. For example Infura endpoint.
     * @return endpoint URL.
     */
    URL getEthereumJsonRpcEndpoint();

    /**
     * Return IPFS RPC endpoint.
     * @return endpoint URL.
     */
    URL getIpfsEndpoint();

    /**
     * Return Ethereum identity type.
     * @return identity type.
     */
    IdentityType getIdentityType();

    /**
     * Return Ethereum identity mnemonic. Applicable when identity type is
     * MNEMONIC.
     * @return identity mnemonic.
     */
    Optional<String> getIdentityMnemonic();
    
    /**
     * Return Ethereum identity private key. Applicable when identity type is
     * PRIVATE_KEY.
     * @return private key bytes.
     */
    Optional<byte[]> getIdentityPrivateKey();

    /**
     * Return Registry contract address. It can be useful if SDK is used with
     * local Ethereum network. Optional.
     * @return address of the Registry contract.
     */
    Optional<Address> getRegistryAddress();

    /**
     * Return MultiPartyEscrow contract address. It can be useful if SDK is
     * used with local Ethereum network. Optional.
     * @return address of the MultiPartyEscrow contract.
     */
    Optional<Address> getMultiPartyEscrowAddress();

    /**
     * Return Ethereum gas price. Default gas price is retrieved from web3j
     * library. At the moment it is about 4 gwei.
     * @return Ethereum gas price in wei.
     */
    Optional<BigInteger> getGasPrice();

    /**
     * Return Ethereum gas limit. Default gas limit is retrieved from web3j
     * library. At the moment it is 9 000 000 units.
     * @return Ethereum gas limit.
     */
    Optional<BigInteger> getGasLimit();

}
