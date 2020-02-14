package io.singularitynet.sdk.client;

import org.junit.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Optional;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.client.Configuration.IdentityType;

public class ConfigurationUtilsTest {

    @Test
    public void fromJsonLoadAllProperties() throws MalformedURLException {
        String ethereumJsonRpcEndpoint = "http://localhost:8545";
        String ipfsEndpoint = "http://localhost:5002";
        String privateKey = "01020304050607FAFBFCFDFEFF";
        String registryAddress = "0x663422c6999Ff94933DBCb388623952CF2407F6f";
        String mpeAddress = "0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C";
        String json = "{" +
            "\"ethereum_json_rpc_endpoint\": \"" + ethereumJsonRpcEndpoint + "\"," +
            "\"ipfs_endpoint\": \"" + ipfsEndpoint + "\"," +
            "\"identity_type\": \"PRIVATE_KEY\"," +
            "\"identity_private_key_hex\": \"" + privateKey + "\"," +
            "\"registry_address\": \"" + registryAddress + "\"," +
            "\"multi_party_escrow_address\": \"" + mpeAddress + "\"," +
            "\"gas_price\": \"1000000\"," +
            "\"gas_limit\": \"2000000\"" +
            "}";

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("Ethereum JSON RPC endpoint", new URL(ethereumJsonRpcEndpoint), config.getEthereumJsonRpcEndpoint());
        assertEquals("IPFS endpoint", new URL(ipfsEndpoint), config.getIpfsEndpoint());
        assertEquals("Identity type", IdentityType.PRIVATE_KEY, config.getIdentityType());
        assertArrayEquals("Identity private key", Utils.hexToBytes(privateKey), config.getIdentityPrivateKey().get());
        assertEquals("Registry address", new Address(registryAddress), config.getRegistryAddress().get());
        assertEquals("MultiPartyEscrow address", new Address(mpeAddress), config.getMultiPartyEscrowAddress().get());
        assertEquals("Gas price", BigInteger.valueOf(1000000), config.getGasPrice().get());
        assertEquals("Gas limit", BigInteger.valueOf(2000000), config.getGasLimit().get());
    }

    @Test
    public void fromJsonLoadAllRequiredProperties() throws MalformedURLException {
        String ethereumJsonRpcEndpoint = "http://localhost:8545";
        String ipfsEndpoint = "http://localhost:5002";
        String json = "{" +
            "\"ethereum_json_rpc_endpoint\": \"" + ethereumJsonRpcEndpoint + "\"," +
            "\"ipfs_endpoint\": \"" + ipfsEndpoint + "\"," +
            "\"identity_type\": \"PRIVATE_KEY\"" +
            "}";

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("Ethereum JSON RPC endpoint", new URL(ethereumJsonRpcEndpoint), config.getEthereumJsonRpcEndpoint());
        assertEquals("IPFS endpoint", new URL(ipfsEndpoint), config.getIpfsEndpoint());
        assertEquals("Identity type", IdentityType.PRIVATE_KEY, config.getIdentityType());
        assertEquals("Identity private key", Optional.empty(), config.getIdentityPrivateKey());
        assertEquals("Registry address", Optional.empty(), config.getRegistryAddress());
        assertEquals("MultiPartyEscrow address", Optional.empty(), config.getMultiPartyEscrowAddress());
        assertEquals("Gas price", Optional.empty(), config.getGasPrice());
        assertEquals("Gas limit", Optional.empty(), config.getGasLimit());
    }

}
