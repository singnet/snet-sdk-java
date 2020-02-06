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
            .setIdentityType(Enum.valueOf(Configuration.IdentityType.class, props.getProperty("identity.type").toUpperCase()));

        Optional.ofNullable(props.getProperty("identity.mnemonic")).ifPresent(builder::setIdentityMnemonic);
        Optional.ofNullable(props.getProperty("identity.private.key.hex")).map(Utils::hexToBytes).ifPresent(builder::setIdentityPrivateKey);
        Optional.ofNullable(props.getProperty("registry.address")).map(Address::new).ifPresent(builder::setRegistryAddress);
        Optional.ofNullable(props.getProperty("multi.party.escrow.address")).map(Address::new).ifPresent(builder::setMultiPartyEscrowAddress);

        return builder.build();
    }

    private static final class JsonConfiguration {
        URL ethereumJsonRpcEndpoint;
        URL ipfsEndpoint;
        String identityType;
        String identityMnemonic;
        String identityPrivateKeyHex;
        String registryAddress;
        String multiPartyEscrowAddress;
    }

    public static Configuration fromJson(String json) {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        JsonConfiguration config = gson.fromJson(json, JsonConfiguration.class);

        StaticConfiguration.Builder builder = StaticConfiguration.newBuilder()
            .setEthereumJsonRpcEndpoint(config.ethereumJsonRpcEndpoint)
            .setIpfsEndpoint(config.ipfsEndpoint)
            .setIdentityType(Enum.valueOf(Configuration.IdentityType.class, config.identityType.toUpperCase()));

        Optional.ofNullable(config.identityMnemonic).ifPresent(builder::setIdentityMnemonic);
        Optional.ofNullable(config.identityPrivateKeyHex).map(Utils::hexToBytes).ifPresent(builder::setIdentityPrivateKey);
        Optional.ofNullable(config.registryAddress).map(Address::new).ifPresent(builder::setRegistryAddress);
        Optional.ofNullable(config.multiPartyEscrowAddress).map(Address::new).ifPresent(builder::setMultiPartyEscrowAddress);

        return builder.build();
    }

}
