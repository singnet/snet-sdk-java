package io.singularitynet.sdk.client;

import org.junit.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.Properties;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.client.Configuration.IdentityType;

public class ConfigurationUtilsTest {

    private final static String ethereumJsonRpcEndpoint = "http://localhost:8545";
    private final static String ipfsEndpoint = "http://localhost:5002";
    private final static String identityType = "PRIVATE_KEY";
    private final static String privateKey = "01020304050607FAFBFCFDFEFF";
    private final static String registryAddress = "0x663422c6999Ff94933DBCb388623952CF2407F6f";
    private final static String mpeAddress = "0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C";
    private final static String gasPrice = "1000000";
    private final static String gasLimit = "2000000";

    @Test
    public void fromJsonLoadAllProperties() throws MalformedURLException {
        String json = "{" +
            "\"ethereum_json_rpc_endpoint\": \"" + ethereumJsonRpcEndpoint + "\"," +
            "\"ipfs_endpoint\": \"" + ipfsEndpoint + "\"," +
            "\"identity_type\": \"" + identityType + "\"," +
            "\"identity_private_key_hex\": \"" + privateKey + "\"," +
            "\"registry_address\": \"" + registryAddress + "\"," +
            "\"multi_party_escrow_address\": \"" + mpeAddress + "\"," +
            "\"gas_price\": \"" + gasPrice + "\"," +
            "\"gas_limit\": \"" + gasLimit + "\"" +
            "}";

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("Ethereum JSON RPC endpoint", new URL(ethereumJsonRpcEndpoint), config.getEthereumJsonRpcEndpoint());
        assertEquals("IPFS endpoint", new URL(ipfsEndpoint), config.getIpfsEndpoint());
        assertEquals("Identity type", Enum.valueOf(Configuration.IdentityType.class, identityType), config.getIdentityType());
        assertArrayEquals("Identity private key", Utils.hexToBytes(privateKey), config.getIdentityPrivateKey().get());
        assertEquals("Registry address", new Address(registryAddress), config.getRegistryAddress().get());
        assertEquals("MultiPartyEscrow address", new Address(mpeAddress), config.getMultiPartyEscrowAddress().get());
        assertEquals("Gas price", new BigInteger(gasPrice), config.getGasPrice());
        assertEquals("Gas limit", new BigInteger(gasLimit), config.getGasLimit());
    }

    @Test
    public void fromJsonLoadAllRequiredProperties() throws MalformedURLException {
        String json = "{" +
            "\"ethereum_json_rpc_endpoint\": \"" + ethereumJsonRpcEndpoint + "\"," +
            "\"identity_type\": \"" + identityType + "\"" +
            "}";

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("Ethereum JSON RPC endpoint", new URL(ethereumJsonRpcEndpoint), config.getEthereumJsonRpcEndpoint());
        assertEquals("IPFS endpoint", Configuration.DEFAULT_IPFS_ENDPOINT, config.getIpfsEndpoint());
        assertEquals("Identity type", Enum.valueOf(Configuration.IdentityType.class, identityType), config.getIdentityType());
        assertEquals("Identity private key", Optional.empty(), config.getIdentityPrivateKey());
        assertEquals("Registry address", Optional.empty(), config.getRegistryAddress());
        assertEquals("MultiPartyEscrow address", Optional.empty(), config.getMultiPartyEscrowAddress());
        assertEquals("Gas price", Configuration.DEFAULT_GAS_PRICE, config.getGasPrice());
        assertEquals("Gas limit", Configuration.DEFAULT_GAS_LIMIT, config.getGasLimit());
    }

    @Test
    public void fromPropertiesLoadAllProperties() throws MalformedURLException {
        Properties props = new Properties();
        props.setProperty("ethereum.json.rpc.endpoint", ethereumJsonRpcEndpoint);
        props.setProperty("ipfs.endpoint", ipfsEndpoint);
        props.setProperty("identity.type", identityType);
        props.setProperty("identity.private.key.hex", privateKey);
        props.setProperty("registry.address", registryAddress);
        props.setProperty("multi.party.escrow.address", mpeAddress);
        props.setProperty("gas.price", gasPrice);
        props.setProperty("gas.limit", gasLimit);

        Configuration config = ConfigurationUtils.fromProperties(props);

        assertEquals("Ethereum JSON RPC endpoint", new URL(ethereumJsonRpcEndpoint), config.getEthereumJsonRpcEndpoint());
        assertEquals("IPFS endpoint", new URL(ipfsEndpoint), config.getIpfsEndpoint());
        assertEquals("Identity type", Enum.valueOf(Configuration.IdentityType.class, identityType), config.getIdentityType());
        assertArrayEquals("Identity private key", Utils.hexToBytes(privateKey), config.getIdentityPrivateKey().get());
        assertEquals("Registry address", new Address(registryAddress), config.getRegistryAddress().get());
        assertEquals("MultiPartyEscrow address", new Address(mpeAddress), config.getMultiPartyEscrowAddress().get());
        assertEquals("Gas price", new BigInteger(gasPrice), config.getGasPrice());
        assertEquals("Gas limit", new BigInteger(gasLimit), config.getGasLimit());
    }

    @Test
    public void fromPropertiesLoadAllRequiredProperties() throws MalformedURLException {
        Properties props = new Properties();
        props.setProperty("ethereum.json.rpc.endpoint", ethereumJsonRpcEndpoint);
        props.setProperty("identity.type", identityType);

        Configuration config = ConfigurationUtils.fromProperties(props);

        assertEquals("Ethereum JSON RPC endpoint", new URL(ethereumJsonRpcEndpoint), config.getEthereumJsonRpcEndpoint());
        assertEquals("IPFS endpoint", Configuration.DEFAULT_IPFS_ENDPOINT, config.getIpfsEndpoint());
        assertEquals("Identity type", Enum.valueOf(Configuration.IdentityType.class, identityType), config.getIdentityType());
        assertEquals("Identity private key", Optional.empty(), config.getIdentityPrivateKey());
        assertEquals("Registry address", Optional.empty(), config.getRegistryAddress());
        assertEquals("MultiPartyEscrow address", Optional.empty(), config.getMultiPartyEscrowAddress());
        assertEquals("Gas price", Configuration.DEFAULT_GAS_PRICE, config.getGasPrice());
        assertEquals("Gas limit", Configuration.DEFAULT_GAS_LIMIT, config.getGasLimit());
    }
}
