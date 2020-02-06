package io.singularitynet.sdk.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import io.singularitynet.sdk.ethereum.Address;

@EqualsAndHashCode
@ToString
public class StaticConfiguration implements Configuration {

    private final URL ethereumJsonRpcEndpoint;
    private final URL ipfsEndpoint;
    private final IdentityType identityType;
    private final Optional<String> identityMnemonic;
    private final Optional<byte[]> identityPrivateKey;
    private final Optional<Address> registryAddress;
    private final Optional<Address> multiPartyEscrowAddress;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private StaticConfiguration(Builder builder) {
        this.ethereumJsonRpcEndpoint = builder.ethereumJsonRpcEndpoint;
        this.ipfsEndpoint = builder.ipfsEndpoint;
        this.identityType = builder.identityType;
        this.identityMnemonic = builder.identityMnemonic;
        this.identityPrivateKey = builder.identityPrivateKey;
        this.registryAddress = builder.registryAddress;
        this.multiPartyEscrowAddress = builder.multiPartyEscrowAddress;
    }

    public URL getEthereumJsonRpcEndpoint() {
        return ethereumJsonRpcEndpoint;
    }

    public URL getIpfsEndpoint() {
        return ipfsEndpoint;
    }

    public IdentityType getIdentityType() {
        return identityType;
    }

    public Optional<String> getIdentityMnemonic() {
        return identityMnemonic;
    }

    public Optional<byte[]> getIdentityPrivateKey() {
        return identityPrivateKey;
    }

    public Optional<Address> getRegistryAddress() {
        return registryAddress;
    }

    public Optional<Address> getMultiPartyEscrowAddress() {
        return multiPartyEscrowAddress;
    }

    public static class Builder {

        private URL ethereumJsonRpcEndpoint;
        private URL ipfsEndpoint;
        private IdentityType identityType;
        private Optional<String> identityMnemonic;
        private Optional<byte[]> identityPrivateKey;
        private Optional<Address> registryAddress;
        private Optional<Address> multiPartyEscrowAddress;

        private Builder() {
            this.identityMnemonic = Optional.<String>empty();
            this.identityPrivateKey = Optional.<byte[]>empty();
            this.registryAddress = Optional.<Address>empty();
            this.multiPartyEscrowAddress = Optional.<Address>empty();
        }

        private Builder(StaticConfiguration object) {
            this.ethereumJsonRpcEndpoint = object.ethereumJsonRpcEndpoint;
            this.ipfsEndpoint = object.ipfsEndpoint;
            this.identityType = object.identityType;
            this.identityMnemonic = object.identityMnemonic;
            this.identityPrivateKey = object.identityPrivateKey;
            this.registryAddress = object.registryAddress;
            this.multiPartyEscrowAddress = object.multiPartyEscrowAddress;
        }

        public Builder setEthereumJsonRpcEndpoint(URL ethereumJsonRpcEndpoint) {
            this.ethereumJsonRpcEndpoint = ethereumJsonRpcEndpoint;
            return this;
        }

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

        public Builder setIpfsEndpoint(URL ipfsEndpoint) {
            this.ipfsEndpoint = ipfsEndpoint;
            return this;
        }

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

        public Builder setIdentityType(IdentityType identityType) {
            this.identityType = identityType;
            return this;
        }

        public IdentityType getIdentityType() {
            return identityType;
        }

        public Builder setIdentityMnemonic(String identityMnemonic) {
            this.identityMnemonic = Optional.of(identityMnemonic);
            return this;
        }

        public Optional<String> getIdentityMnemonic() {
            return identityMnemonic;
        }

        public Builder setIdentityPrivateKey(byte[] identityPrivateKey) {
            this.identityPrivateKey = Optional.of(identityPrivateKey);
            return this;
        }

        public Optional<byte[]> getIdentityPrivateKey() {
            return identityPrivateKey;
        }

        public Builder setRegistryAddress(Address registryAddress) {
            this.registryAddress = Optional.of(registryAddress);
            return this;
        }

        public Optional<Address> getRegistryAddress() {
            return registryAddress;
        }

        public Builder setMultiPartyEscrowAddress(Address multiPartyEscrowAddress) {
            this.multiPartyEscrowAddress = Optional.of(multiPartyEscrowAddress);
            return this;
        }

        public Optional<Address> getMultiPartyEscrowAddress() {
            return multiPartyEscrowAddress;
        }

        public StaticConfiguration build() {
            return new StaticConfiguration(this);
        }
    }

}
