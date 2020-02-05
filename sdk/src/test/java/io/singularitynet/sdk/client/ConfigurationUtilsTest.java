package io.singularitynet.sdk.client;

import org.junit.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Optional;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.client.Configuration.IdentityType;

public class ConfigurationUtilsTest {

    private static String ENDPOINTS =
            "\"ethereum_json_rpc_endpoint\": \"http://localhost:8545\"," +
            "\"ipfs_endpoint\": \"http://localhost:5002\",";

    private static String SIGNER =
            "\"signer_type\": \"PRIVATE_KEY\"," +
            "\"signer_private_key_hex\": \"010203\",";

    private static String HEADER = "{" +
            "\"ethereum_json_rpc_endpoint\": \"http://localhost:8545\"," +
            "\"ipfs_endpoint\": \"http://localhost:5002\"," +
            "\"signer_type\": \"PRIVATE_KEY\"," +
            "\"signer_private_key_hex\": \"010203\"" +
            "}";

    @Test
    public void fromJsonConfigureEthereumJsonRpcEndpoint() throws MalformedURLException {
        String ethereumJsonRpcEndpoint = "https://ropsten.infura.io";
        String json = "{" + SIGNER +
            "\"ethereum_json_rpc_endpoint\": \"" + ethereumJsonRpcEndpoint + "\"," +
            "\"ipfs_endpoint\": \"http://localhost:5002\"" +
            "}";

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("Ethereum JSON RPC endpoint", new URL(ethereumJsonRpcEndpoint), config.getEthereumJsonRpcEndpoint());
    }

    @Test
    public void fromJsonConfigureIpfsEndpoint() throws MalformedURLException {
        String ipfsEndpoint = "http://ipfs.singularitynet.io:80";
        String json = "{" + SIGNER +
            "\"ethereum_json_rpc_endpoint\": \"http://localhost:8545\"," +
            "\"ipfs_endpoint\": \"" + ipfsEndpoint + "\"" +
            "}";

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("IPFS URL", new URL(ipfsEndpoint), config.getIpfsEndpoint());
    }

    @Test
    public void fromJsonConfigureSignerPrivateKey() {
        byte[] privateKey = Utils.base64ToBytes("1PeCDRD7vLjqiGoHl7A+yPuJIy8TdbNc1vxOyuPjxBM=");
        String json = "{" + ENDPOINTS +
            "\"signer_type\": \"PRIVATE_KEY\"," +
            "\"signer_private_key_hex\": \"" + Utils.bytesToHex(privateKey) + "\"" +
            "}";

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("Signer type", IdentityType.PRIVATE_KEY, config.getSignerType());
        assertArrayEquals("Signer private key", privateKey, config.getSignerPrivateKey().get());
    }

    @Test
    public void fromJsonConfigureRegistryAddress() {
        String registryAddress = "0x663422c6999Ff94933DBCb388623952CF2407F6f";
        String json = "{" + ENDPOINTS + SIGNER +
            "\"registry_address\": \"" + registryAddress + "\"" +
            "}";

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("Registry address", Optional.of(new Address(registryAddress)), config.getRegistryAddress());
    }

    @Test
    public void fromJsonConfigureDefaultRegistryAddress() {
        String json = HEADER;

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("Registry address", Optional.<Address>empty(), config.getRegistryAddress());
    }

    @Test
    public void fromJsonConfigureMultiPartyEscrowAddress() {
        String mpeAddress = "0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C";
        String json = "{" + ENDPOINTS + SIGNER +
            "\"multi_party_escrow_address\": \"" + mpeAddress + "\"" +
            "}";

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("MultiPartyEscrow address", Optional.of(new Address(mpeAddress)), config.getMultiPartyEscrowAddress());
    }

    @Test
    public void fromJsonConfigureDefaultMultiPartyEscrowAddress() {
        String json = HEADER;

        Configuration config = ConfigurationUtils.fromJson(json);

        assertEquals("MultiPartyEscrow address", Optional.<Address>empty(), config.getMultiPartyEscrowAddress());
    }

}
