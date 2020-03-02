package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Random;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;
import io.singularitynet.sdk.ethereum.Signature;
import io.singularitynet.sdk.freecall.FreeCallPayment;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.FreeCallPaymentStrategy;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.ServiceClient;

public class FreeCallPaymentStrategyTestIT {

    private Web3j web3j;

    @Before
    public void setUp() {
        web3j = Web3j.build(new HttpService(IntEnv.ETHEREUM_JSON_RPC_ENDPOINT));
    }

    @After
    public void tearDown() {
        web3j.shutdown();
    }

    @Test
    public void useFreeCallPayment() {
        final String dappUserId = randomDappUserId();
        final Address userEthereumAddress = IntEnv.CALLER_ADDRESS;
        final BigInteger freeCallExpirationBlock = BigInteger.valueOf(1)
            .add(getLastEthereumBlock());
        final String freeCallToken = FreeCallPayment.generateFreeCallPaymentToken(
                dappUserId, userEthereumAddress, freeCallExpirationBlock,
                IntEnv.DEPLOYER_IDENTITY);

        Configuration config = IntEnv.newTestConfigurationBuilder()
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(IntEnv.CALLER_PRIVATE_KEY)
            .build();
        Sdk sdk = new Sdk(config);
        try {

            Identity freeCallSigner = sdk.getIdentity();
            FreeCallPaymentStrategy freeCallStrategy = new FreeCallPaymentStrategy(
                    sdk.getEthereum(), freeCallSigner, dappUserId,
                    freeCallExpirationBlock, freeCallToken);
            ServiceClient serviceClient = sdk.newServiceClient(
                    IntEnv.TEST_ORG_ID, IntEnv.TEST_SERVICE_ID,
                    IntEnv.TEST_ENDPOINT_GROUP, freeCallStrategy);
            try {

               IntEnv.makeServiceCall(serviceClient); 

            } finally {
                serviceClient.shutdownNow();
            }

        } finally {
            sdk.shutdown();
        }
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // FIXME: remove code duplication with previous test
    @Test
    public void freeCallRejectedWhenNotConfigured() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("No payment returned by PaymentStrategy");

        final String dappUserId = randomDappUserId();
        final Address userEthereumAddress = IntEnv.CALLER_ADDRESS;
        final BigInteger freeCallExpirationBlock = BigInteger.valueOf(1)
            .add(getLastEthereumBlock());
        final String freeCallToken = FreeCallPayment.generateFreeCallPaymentToken(
                dappUserId, userEthereumAddress, freeCallExpirationBlock,
                IntEnv.DEPLOYER_IDENTITY);

        Configuration config = IntEnv.newTestConfigurationBuilder()
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(IntEnv.CALLER_PRIVATE_KEY)
            .build();
        Sdk sdk = new Sdk(config);
        try {

            Identity freeCallSigner = sdk.getIdentity();
            FreeCallPaymentStrategy freeCallStrategy = new FreeCallPaymentStrategy(
                    sdk.getEthereum(), freeCallSigner, dappUserId,
                    freeCallExpirationBlock, freeCallToken);
            ServiceClient serviceClient = sdk.newServiceClient(
                    IntEnv.TEST_ORG_ID, IntEnv.TEST_SERVICE_ID,
                    IntEnv.TEST_ENDPOINT_GROUP, freeCallStrategy);
            boolean twoCalls = false;
            try {

               IntEnv.makeServiceCall(serviceClient); 
               IntEnv.makeServiceCall(serviceClient); 
               twoCalls = true;
               IntEnv.makeServiceCall(serviceClient); 

            } finally {
                assertTrue("Two calls made successfully", twoCalls);
                serviceClient.shutdownNow();
            }

        } finally {
            sdk.shutdown();
        }
    }

    private BigInteger getLastEthereumBlock() {
        return Utils.wrapExceptions(() -> web3j.ethBlockNumber().send()
                .getBlockNumber());
    }

    private static final Random random = new Random();

    private static String randomDappUserId() {
        return "user-" + random.nextInt() + "@mail.com";
    }

}
