package io.singularitynet.sdk.integration;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.StaticConfiguration;

public class IntEnv {

    public static final String ETHEREUM_JSON_RPC_ENDPOINT = "http://localhost:8545";
    public static final String IPFS_ENDPOINT = "http://localhost:5002";

    public static final Address REGISTRY_CONTRACT_ADDRESS = new Address("0x4e74fefa82e83e0964f0d9f53c68e03f7298a8b2");
    public static final Address MPE_CONTRACT_ADDRESS = new Address("0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e");

    public static final byte[] DEPLOYER_PRIVATE_KEY = Utils.hexToBytes("c71478a6d0fe44e763649de0a0deb5a080b788eefbbcf9c6f7aef0dd5dbd67e0");
    public static final PrivateKeyIdentity DEPLOYER_IDENTITY = new PrivateKeyIdentity(DEPLOYER_PRIVATE_KEY);
    public static final byte[] CALLER_PRIVATE_KEY = Utils.hexToBytes("04899d5fd471ce68f84a5ec64e2e4b6b045d8b850599a57f5b307024be01f262");
    public static final Address CALLER_ADDRESS = new Address("0x3b2b3C2e2E7C93db335E69D827F3CC4bC2A2A2cB");

    public static final String TEST_ORG_ID = "example-org";
    public static final String TEST_SERVICE_ID = "example-service";
    public static final String TEST_ENDPOINT_GROUP = "default_group";

    // FIXME: replace by newTestConfigurationBuilder() call
    public static final StaticConfiguration.Builder TEST_CONFIGURATION_BUILDER = newTestConfigurationBuilder();

    private IntEnv() {
    }

    public static StaticConfiguration.Builder newTestConfigurationBuilder() {
        return StaticConfiguration.newBuilder()
            .setEthereumJsonRpcEndpoint(ETHEREUM_JSON_RPC_ENDPOINT)
            .setIpfsEndpoint(IPFS_ENDPOINT)
            .setRegistryAddress(REGISTRY_CONTRACT_ADDRESS)
            .setMultiPartyEscrowAddress(MPE_CONTRACT_ADDRESS);
    }

}
