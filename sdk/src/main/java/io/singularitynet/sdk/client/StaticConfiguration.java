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
    private final IdentityType signerType;
    private final Optional<String> signerMnemonic;
    private final Optional<byte[]> signerPrivateKey;
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
        this.signerType = builder.signerType;
        this.signerMnemonic = builder.signerMnemonic;
        this.signerPrivateKey = builder.signerPrivateKey;
        this.registryAddress = builder.registryAddress;
        this.multiPartyEscrowAddress = builder.multiPartyEscrowAddress;
    }

    public URL getEthereumJsonRpcEndpoint() {
        return ethereumJsonRpcEndpoint;
    }

    public URL getIpfsEndpoint() {
        return ipfsEndpoint;
    }

    public IdentityType getSignerType() {
        return signerType;
    }

    public Optional<String> getSignerMnemonic() {
        return signerMnemonic;
    }

    public Optional<byte[]> getSignerPrivateKey() {
        return signerPrivateKey;
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
        private IdentityType signerType;
        private Optional<String> signerMnemonic;
        private Optional<byte[]> signerPrivateKey;
        private Optional<Address> registryAddress;
        private Optional<Address> multiPartyEscrowAddress;

        private Builder() {
            this.signerMnemonic = Optional.<String>empty();
            this.signerPrivateKey = Optional.<byte[]>empty();
            this.registryAddress = Optional.<Address>empty();
            this.multiPartyEscrowAddress = Optional.<Address>empty();
        }

        private Builder(StaticConfiguration object) {
            this.ethereumJsonRpcEndpoint = object.ethereumJsonRpcEndpoint;
            this.ipfsEndpoint = object.ipfsEndpoint;
            this.signerType = object.signerType;
            this.signerMnemonic = object.signerMnemonic;
            this.signerPrivateKey = object.signerPrivateKey;
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

        public Builder setSignerType(IdentityType signerType) {
            this.signerType = signerType;
            return this;
        }

        public IdentityType getSignerType() {
            return signerType;
        }

        public Builder setSignerMnemonic(String signerMnemonic) {
            this.signerMnemonic = Optional.of(signerMnemonic);
            return this;
        }

        public Optional<String> getSignerMnemonic() {
            return signerMnemonic;
        }

        public Builder setSignerPrivateKey(byte[] signerPrivateKey) {
            this.signerPrivateKey = Optional.of(signerPrivateKey);
            return this;
        }

        public Optional<byte[]> getSignerPrivateKey() {
            return signerPrivateKey;
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
