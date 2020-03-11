package io.singularitynet.sdk.client;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;

/**
 * SDK configuration implementation. Provides Builder API to set and return
 * configuration values.
 */
@EqualsAndHashCode
@ToString
public class Configuration {

    /**
     * Mainnet Infura Ethereum JSON RPC endpoint using SingularityNet
     * project id.
     */
    public static final URL MAINNET_INFURA_ETHEREUM_JSON_RPC_ENDPOINT = Utils.strToUrl("https://mainnet.infura.io/v3/e7732e1f679e461b9bb4da5653ac3fc2");

    /**
     * Ropsten Infura Ethereum JSON RPC endpoint using SingularityNet
     * project id.
     */
    public static final URL ROPSTEN_INFURA_ETHEREUM_JSON_RPC_ENDPOINT = Utils.strToUrl("https://ropsten.infura.io/v3/e7732e1f679e461b9bb4da5653ac3fc2");

    /**
     * Kovan Infura Ethereum JSON RPC endpoint using SingularityNet
     * project id.
     */
    public static final URL KOVAN_INFURA_ETHEREUM_JSON_RPC_ENDPOINT = Utils.strToUrl("https://kovan.infura.io/v3/e7732e1f679e461b9bb4da5653ac3fc2");

    /**
     * Default IPFS endpoint.
     */
    public static final URL DEFAULT_IPFS_ENDPOINT = Utils.strToUrl("http://ipfs.singularitynet.io:80");

    /**
     * Default Ethereum gas price in wei.
     */
    public static final BigInteger DEFAULT_GAS_PRICE = new BigInteger("6100000000");

    /**
     * Default Ethereum gas limit in units.
     */
    public static final BigInteger DEFAULT_GAS_LIMIT = new BigInteger("200000");

    /**
     * Type of the identity.
     */
    public static enum IdentityType {
        /**
         * Mnemonic identity.
         */
        MNEMONIC,
        /**
         * Explicit private key identity
         */
        PRIVATE_KEY
    }

    private final URL ethereumJsonRpcEndpoint;
    private final URL ipfsEndpoint;
    private final IdentityType identityType;
    private final Optional<String> identityMnemonic;
    private final Optional<byte[]> identityPrivateKey;
    private final Optional<Address> registryAddress;
    private final Optional<Address> multiPartyEscrowAddress;
    private final BigInteger gasPrice;
    private final BigInteger gasLimit;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private Configuration(Builder builder) {
        this.ethereumJsonRpcEndpoint = builder.ethereumJsonRpcEndpoint;
        this.ipfsEndpoint = builder.ipfsEndpoint;
        this.identityType = builder.identityType;
        this.identityMnemonic = builder.identityMnemonic;
        this.identityPrivateKey = builder.identityPrivateKey;
        this.registryAddress = builder.registryAddress;
        this.multiPartyEscrowAddress = builder.multiPartyEscrowAddress;
        this.gasPrice = builder.gasPrice;
        this.gasLimit = builder.gasLimit;
    }

    /**
     * @return Ethereum JSON RPC endpoint URL of the selected network.
     */
    public URL getEthereumJsonRpcEndpoint() {
        return ethereumJsonRpcEndpoint;
    }

    /**
     * @return IPFS endpoint URL.
     */
    public URL getIpfsEndpoint() {
        return ipfsEndpoint;
    }

    /**
     * @return Ethereum identity type.
     */
    public IdentityType getIdentityType() {
        return identityType;
    }

    /**
     * @return Ethereum identity mnemonic.
     */
    public Optional<String> getIdentityMnemonic() {
        return identityMnemonic;
    }

    /**
     * @return Ethereum identity private key bytes.
     */
    public Optional<byte[]> getIdentityPrivateKey() {
        return identityPrivateKey;
    }

    /**
     * @return address of the Registry contract.
     */
    public Optional<Address> getRegistryAddress() {
        return registryAddress;
    }

    /**
     * @return address of the MultiPartyEscrow contract.
     */
    public Optional<Address> getMultiPartyEscrowAddress() {
        return multiPartyEscrowAddress;
    }

    /**
     * @return Ethereum gas price in wei.
     */
    public BigInteger getGasPrice() {
        return gasPrice;
    }

    /**
     * @return Ethereum gas limit in units.
     */
    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public static class Builder {

        private URL ethereumJsonRpcEndpoint;
        private URL ipfsEndpoint;
        private IdentityType identityType;
        private Optional<String> identityMnemonic;
        private Optional<byte[]> identityPrivateKey;
        private Optional<Address> registryAddress;
        private Optional<Address> multiPartyEscrowAddress;
        private BigInteger gasPrice;
        private BigInteger gasLimit;

        private Builder() {
            this.ipfsEndpoint = DEFAULT_IPFS_ENDPOINT;
            this.identityMnemonic = Optional.<String>empty();
            this.identityPrivateKey = Optional.<byte[]>empty();
            this.registryAddress = Optional.<Address>empty();
            this.multiPartyEscrowAddress = Optional.<Address>empty();
            this.gasPrice = DEFAULT_GAS_PRICE; 
            this.gasLimit = DEFAULT_GAS_LIMIT;
        }

        private Builder(Configuration object) {
            this.ethereumJsonRpcEndpoint = object.ethereumJsonRpcEndpoint;
            this.ipfsEndpoint = object.ipfsEndpoint;
            this.identityType = object.identityType;
            this.identityMnemonic = object.identityMnemonic;
            this.identityPrivateKey = object.identityPrivateKey;
            this.registryAddress = object.registryAddress;
            this.multiPartyEscrowAddress = object.multiPartyEscrowAddress;
            this.gasPrice = object.gasPrice;
            this.gasLimit = object.gasLimit;
        }

        /**
         * Required. Set Ethereum JSON RPC endpoint to select Ethereum network.
         * For example Infura endpoint can be used as a value.
         * @param ethereumJsonRpcEndpoint Etherum JSON RPC endpoint URL.
         * @return builder.
         */
        public Builder setEthereumJsonRpcEndpoint(URL ethereumJsonRpcEndpoint) {
            this.ethereumJsonRpcEndpoint = ethereumJsonRpcEndpoint;
            return this;
        }

        /**
         * Required. Set Ethereum JSON RPC endpoint as a string to select
         * Ethereum network.  For example Infura endpoint can be used as a
         * value.
         * @param ethereumJsonRpcEndpoint Etherum JSON RPC endpoint URL.
         * @return builder.
         */
        public Builder setEthereumJsonRpcEndpoint(String ethereumJsonRpcEndpoint) {
            try {
                this.ethereumJsonRpcEndpoint = new URL(ethereumJsonRpcEndpoint);
            } catch(MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
            return this;
        }

        public URL getEthereumJsonRpcEndpoint() {
            return ethereumJsonRpcEndpoint;
        }

        /**
         * Optional. Set IPFS RPC endpoint. Default endpoint is a value of
         * Configuration.DEFAULT_IPFS_ENDPOINT.
         * @param ipfsEndpoint endpoint URL.
         * @return builder.
         */
        public Builder setIpfsEndpoint(URL ipfsEndpoint) {
            this.ipfsEndpoint = ipfsEndpoint;
            return this;
        }

        /**
         * Optional. Set IPFS RPC endpoint as a String. Default endpoint is a
         * value of Configuration.DEFAULT_IPFS_ENDPOINT.
         * @param ipfsEndpoint endpoint URL as a string.
         * @return builder.
         */
        public Builder setIpfsEndpoint(String ipfsEndpoint) {
            try {
                this.ipfsEndpoint = new URL(ipfsEndpoint);
            } catch(MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
            return this;
        }

        public URL getIpfsEndpoint() {
            return ipfsEndpoint;
        }

        /**
         * Required. Set Ethereum identity mnemonic type.
         * @param identityType type of the identity.
         * @return builder.
         */
        public Builder setIdentityType(IdentityType identityType) {
            this.identityType = identityType;
            return this;
        }

        public IdentityType getIdentityType() {
            return identityType;
        }

        /**
         * Optional. Set Ethereum identity mnemonic. Applicable when identity
         * type is MNEMONIC.
         * @param identityMnemonic identity mnemonic.
         * @return builder.
         */
        public Builder setIdentityMnemonic(String identityMnemonic) {
            this.identityMnemonic = Optional.of(identityMnemonic);
            return this;
        }

        public Optional<String> getIdentityMnemonic() {
            return identityMnemonic;
        }

        /**
         * Optional. Set Ethereum identity private key. Applicable when
         * identity type is PRIVATE_KEY.
         * @param identityPrivateKey private key bytes.
         * @return builder.
         */
        public Builder setIdentityPrivateKey(byte[] identityPrivateKey) {
            this.identityPrivateKey = Optional.of(identityPrivateKey);
            return this;
        }

        public Optional<byte[]> getIdentityPrivateKey() {
            return identityPrivateKey;
        }

        /**
         * Optional. Set Registry contract address. Usually it is
         * required only for local Ethereum network. Default address for the
         * current network is used when not set.
         * @param registryAddress Registry contract address.
         * @return builder.
         */
        public Builder setRegistryAddress(Address registryAddress) {
            this.registryAddress = Optional.of(registryAddress);
            return this;
        }

        /**
         * Optional. Set network default Registry contract address.
         * @return builder.
         */
        public Builder setDefaultRegisryAddress() {
            this.registryAddress = Optional.<Address>empty();
            return this;
        }

        public Optional<Address> getRegistryAddress() {
            return registryAddress;
        }

        /**
         * Optional. Set MultiPartyEscrow contract address. Usually it is
         * required only for local Ethereum network. Default address for the
         * current network is used when not set.
         * @param multiPartyEscrowAddress MultiPartyEscrow contract address.
         * @return builder.
         */
        public Builder setMultiPartyEscrowAddress(Address multiPartyEscrowAddress) {
            this.multiPartyEscrowAddress = Optional.of(multiPartyEscrowAddress);
            return this;
        }

        /**
         * Optional. Set network default MultiPartyEscrow contract address.
         * @return builder.
         */
        public Builder setDefaultMultiPartyEscrowAddress() {
            this.multiPartyEscrowAddress = Optional.<Address>empty();
            return this;
        }

        public Optional<Address> getMultiPartyEscrowAddress() {
            return multiPartyEscrowAddress;
        }

        /**
         * Optional. Set Ethereum gas price in wei. Default gas price is a
         * value of Configuration.DEFAULT_GAS_PRICE.
         * @param gasPrice Ethereum gas price in wei.
         * @return builder.
         */
        public Builder setGasPrice(BigInteger gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        public BigInteger getGasPrice() {
            return gasPrice;
        }

        /**
         * Optional. Set Ethereum gas limit. Default gas limit is a value of
         * Configuration.DEFAULT_GAS_LIMIT.
         * @param gasLimit Ethereum gas limit in units.
         * @return builder.
         */
        public Builder setGasLimit(BigInteger gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        public BigInteger getGasLimit() {
            return gasLimit;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }

}
