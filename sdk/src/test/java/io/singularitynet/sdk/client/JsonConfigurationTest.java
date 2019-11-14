package io.singularitynet.sdk.client;

import org.junit.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.net.MalformedURLException;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.client.Configuration.SignerType;

public class JsonConfigurationTest {

    @Test
    public void configureEthereumJsonRpcEndpoint() {
        String ethereumJsonRpcEndpoint = "https://ropsten.infura.io";
        String json = "{" +
            "\"ethereum_json_rpc_endpoint\": \"" + ethereumJsonRpcEndpoint + "\"" +
            "}";

        Configuration config = new JsonConfiguration(json);

        assertEquals("Ethereum JSON RPC endpoint", ethereumJsonRpcEndpoint, config.getEthereumJsonRpcEndpoint());
    }

    @Test
    public void configureIpfsUrl() throws MalformedURLException {
        String ipfsUrl = "http://ipfs.singularitynet.io:80";
        String json = "{" +
            "\"ipfs_url\": \"" + ipfsUrl + "\"" +
            "}";

        Configuration config = new JsonConfiguration(json);

        assertEquals("IPFS URL", new URL(ipfsUrl), config.getIpfsUrl());
    }

    @Test
    public void configureSignerPrivateKey() {
        byte[] privateKey = Utils.base64ToBytes("1PeCDRD7vLjqiGoHl7A+yPuJIy8TdbNc1vxOyuPjxBM=");
        String json = "{" +
            "\"signer_type\": \"PRIVATE_KEY\"," +
            "\"signer_private_key_base64\": \"" + Utils.bytesToBase64(privateKey) + "\"" +
            "}";

        Configuration config = new JsonConfiguration(json);

        assertEquals("Signer type", SignerType.PRIVATE_KEY, config.getSignerType());
        assertArrayEquals("Signer private key", privateKey, config.getSignerPrivateKey());
    }

    @Test
    public void configureRegistryAddress() {
        String registryAddress = "0x663422c6999Ff94933DBCb388623952CF2407F6f";
        String json = "{" +
            "\"registry_address\": \"" + registryAddress + "\"" +
            "}";

        Configuration config = new JsonConfiguration(json);

        assertEquals("Registry address", new Address(registryAddress), config.getRegistryAddress());
    }

    @Test
    public void configureMultiPartyEscrowAddress() {
        String mpeAddress = "0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C";
        String json = "{" +
            "\"multi_party_escrow_address\": \"" + mpeAddress + "\"" +
            "}";

        Configuration config = new JsonConfiguration(json);

        assertEquals("MultiPartyEscrow address", new Address(mpeAddress), config.getMultiPartyEscrowAddress());
    }

}
