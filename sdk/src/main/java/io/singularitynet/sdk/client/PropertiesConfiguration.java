package io.singularitynet.sdk.client;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.Properties;
import lombok.ToString;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;

// FIXME: merge PropertiesConfiguration and JsonConfiguration
@ToString
public class PropertiesConfiguration implements Configuration {

    private final String ethereumJsonRpcEndpoint;
    private final URL ipfsUrl;
    private final SignerType signerType;
    private final String signerMnemonic;
    private final byte[] signerPrivateKey;
    private final Optional<Address> registryAddress;
    private final Optional<Address> multiPartyEscrowAddress;

    public PropertiesConfiguration(Properties props) {
        this.ethereumJsonRpcEndpoint = props.getProperty("ethereum.json.rpc.endpoint");
        try {
            this.ipfsUrl = new URL(props.getProperty("ipfs.url"));
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        this.signerType = Enum.valueOf(SignerType.class, props.getProperty("signer.type").toUpperCase());
        this.signerMnemonic = props.getProperty("signer.mnemonic");
        this.signerPrivateKey = Utils.hexToBytes(props.getProperty("signer.private.key.hex"));
        this.registryAddress = Optional.ofNullable(props.getProperty("registry.address")).map(Address::new);
        this.multiPartyEscrowAddress = Optional.ofNullable(props.getProperty("multi.party.escrow.address")).map(Address::new);
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
        return signerType;
    }

    @Override
    public String getSignerMnemonic() {
        return signerMnemonic;
    }

    @Override
    public byte[] getSignerPrivateKey() {
        return signerPrivateKey;
    }

    @Override
    public Optional<Address> getRegistryAddress() {
        return registryAddress;
    }

    @Override
    public Optional<Address> getMultiPartyEscrowAddress() {
        return multiPartyEscrowAddress;
    }

}
