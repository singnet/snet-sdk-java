package io.singularitynet.sdk.client;

import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;

public class ConfigurationUtils {

    public static Configuration fromProperties(Properties props) {
        StaticConfiguration.Builder builder = StaticConfiguration.newBuilder()
            .setEthereumJsonRpcEndpoint(props.getProperty("ethereum.json.rpc.endpoint"))
            .setIpfsEndpoint(props.getProperty("ipfs.endpoint"))
            .setSignerType(Enum.valueOf(Configuration.SignerType.class, props.getProperty("signer.type").toUpperCase()));

        Optional.ofNullable(props.getProperty("signer.mnemonic")).ifPresent(builder::setSignerMnemonic);
        Optional.ofNullable(props.getProperty("signer.private.key.hex")).map(Utils::hexToBytes).ifPresent(builder::setSignerPrivateKey);
        Optional.ofNullable(props.getProperty("registry.address")).map(Address::new).ifPresent(builder::setRegistryAddress);
        Optional.ofNullable(props.getProperty("multi.party.escrow.address")).map(Address::new).ifPresent(builder::setMultiPartyEscrowAddress);

        return builder.build();
    }

    private static final class JsonConfiguration {
        URL ethereumJsonRpcEndpoint;
        URL ipfsEndpoint;
        String signerType;
        String signerMnemonic;
        String signerPrivateKeyHex;
        String registryAddress;
        String multiPartyEscrowAddress;
    }

    public static Configuration fromJson(String json) {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        JsonConfiguration config = gson.fromJson(json, JsonConfiguration.class);

        StaticConfiguration.Builder builder = StaticConfiguration.newBuilder()
            .setEthereumJsonRpcEndpoint(config.ethereumJsonRpcEndpoint)
            .setIpfsEndpoint(config.ipfsEndpoint)
            .setSignerType(Enum.valueOf(Configuration.SignerType.class, config.signerType.toUpperCase()));

        Optional.ofNullable(config.signerMnemonic).ifPresent(builder::setSignerMnemonic);
        Optional.ofNullable(config.signerPrivateKeyHex).map(Utils::hexToBytes).ifPresent(builder::setSignerPrivateKey);
        Optional.ofNullable(config.registryAddress).map(Address::new).ifPresent(builder::setRegistryAddress);
        Optional.ofNullable(config.multiPartyEscrowAddress).map(Address::new).ifPresent(builder::setMultiPartyEscrowAddress);

        return builder.build();
    }

}
