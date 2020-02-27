package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
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

import io.singularitynet.sdk.test.CalculatorGrpc;
import io.singularitynet.sdk.test.CalculatorGrpc.CalculatorBlockingStub;
import io.singularitynet.sdk.test.ExampleService.Numbers;
import io.singularitynet.sdk.test.ExampleService.Result;

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
        final String dappUserId = "user@mail.com";
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

               makeServiceCall(serviceClient); 

            } finally {
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

    // FIXME: remove code duplication with
    // OnDemandPaymentChannelPaymentStrategyTestIT code
    private static void makeServiceCall(ServiceClient serviceClient) {
        CalculatorBlockingStub stub = serviceClient.getGrpcStub(CalculatorGrpc::newBlockingStub);

        Numbers numbers = Numbers.newBuilder()
            .setA(7)
            .setB(6)
            .build();
        Result result = stub.mul(numbers);

        assertEquals("Result of 6 * 7", Result.newBuilder().setValue(42).build(), result);
    }
}
