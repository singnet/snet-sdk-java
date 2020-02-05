package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import java.util.Properties;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;
import io.singularitynet.sdk.ethereum.MnemonicIdentity;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.mpe.MultiPartyEscrowContract;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.StaticConfiguration;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.OnDemandPaymentChannelPaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;

import io.singularitynet.sdk.test.CalculatorGrpc;
import io.singularitynet.sdk.test.CalculatorGrpc.CalculatorBlockingStub;
import io.singularitynet.sdk.test.ExampleService.Numbers;
import io.singularitynet.sdk.test.ExampleService.Result;

public class PaymentChannelTestIT {

    private static final PrivateKeyIdentity TEST_DEPLOYER = new PrivateKeyIdentity(Utils.hexToBytes("c71478a6d0fe44e763649de0a0deb5a080b788eefbbcf9c6f7aef0dd5dbd67e0"));
    private static final StaticConfiguration TEST_CONFIGURATION = StaticConfiguration.newBuilder()
        .setEthereumJsonRpcEndpoint("http://localhost:8545")
        .setIpfsEndpoint("http://localhost:5002")
        .setSignerType(Configuration.SignerType.PRIVATE_KEY)
        .setSignerPrivateKey(Utils.hexToBytes("04899d5fd471ce68f84a5ec64e2e4b6b045d8b850599a57f5b307024be01f262"))
        .setRegistryAddress(new Address("0x4e74fefa82e83e0964f0d9f53c68e03f7298a8b2"))
        .setMultiPartyEscrowAddress(new Address("0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e"))
        .build();

    private static final String TEST_ORG_ID = "example-org";
    private static final String TEST_SERVICE_ID = "example-service";

    private Web3j web3j;
    private MultiPartyEscrowContract mpe;

    @Before
    public void setUp() {
        this.web3j = Web3j.build(new HttpService(TEST_CONFIGURATION.getEthereumJsonRpcEndpoint().toString()));
        DefaultGasProvider gasProvider = new DefaultGasProvider();
        RawTransactionManager transactionManager = new RawTransactionManager(web3j, TEST_DEPLOYER.getCredentials());
        this.mpe = new MultiPartyEscrowContract(MultiPartyEscrow
                .load(TEST_CONFIGURATION.getMultiPartyEscrowAddress().get().toString(),
                    web3j, transactionManager, gasProvider));
    }

    @After
    public void tearDown() {
        web3j.shutdown();
    }

    @Test
    public void newChannelIsCreatedOnFirstCall() throws Exception {
        PrivateKeyIdentity caller = setupNewIdentity();
        StaticConfiguration config = TEST_CONFIGURATION.toBuilder()
            .setSignerPrivateKey(caller.getCredentials().getEcKeyPair().getPrivateKey().toByteArray())
            .build();
        Sdk sdk = new Sdk(config);
        try {

            PaymentStrategy paymentStrategy = new OnDemandPaymentChannelPaymentStrategy();
            ServiceClient serviceClient = sdk.newServiceClient(TEST_ORG_ID,
                    TEST_SERVICE_ID, "default_group", paymentStrategy); 
            try {

                CalculatorBlockingStub stub = serviceClient.getGrpcStub(CalculatorGrpc::newBlockingStub);

                Numbers numbers = Numbers.newBuilder()
                    .setA(7)
                    .setB(6)
                    .build();
                Result result = stub.mul(numbers);
                assertEquals("Result of 6 * 7", Result.newBuilder().setValue(42).build(), result);

            } finally {
                serviceClient.shutdownNow();
            }

        } finally {
            sdk.shutdown();
        }
    }

    private PrivateKeyIdentity setupNewIdentity() throws Exception {
        PrivateKeyIdentity identity = new MnemonicIdentity("random mnemonic #" + Math.random(), 0);

        Transfer.sendFunds(web3j, TEST_DEPLOYER.getCredentials(), identity.getAddress().toString(),
                BigDecimal.valueOf(1.0), Convert.Unit.ETHER).send();
        // TODO: implement functions to convert cogs and AGIs
        mpe.transfer(identity.getAddress(), BigInteger.valueOf(1000000));

        return identity;
    }

}
