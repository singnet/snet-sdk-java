package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;
import io.singularitynet.sdk.ethereum.MnemonicIdentity;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.registry.PriceModel;
import io.singularitynet.sdk.registry.Pricing;
import io.singularitynet.sdk.registry.PaymentGroup;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.mpe.MultiPartyEscrowContract;
import io.singularitynet.sdk.mpe.PaymentChannel;
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

    private StaticConfiguration.Builder configBuilder;

    private PrivateKeyIdentity deployer;
    private MultiPartyEscrowContract mpe;

    private Sdk sdk;

    private EndpointGroup endpointGroup;
    private PaymentGroup paymentGroup;
    private Pricing servicePrice;
    private BigInteger cogsPerCall;
    private BigInteger expirationThreshold;

    @Before
    public void setUp() {
        this.configBuilder = IntEnv.TEST_CONFIGURATION_BUILDER;
        StaticConfiguration config = configBuilder
            .setSignerType(Configuration.IdentityType.PRIVATE_KEY)
            .setSignerPrivateKey(IntEnv.DEPLOYER_PRIVATE_KEY)
            .build();
        this.sdk = new Sdk(config);

        this.deployer = new PrivateKeyIdentity(IntEnv.DEPLOYER_PRIVATE_KEY);
        DefaultGasProvider gasProvider = new DefaultGasProvider();
        RawTransactionManager transactionManager = new RawTransactionManager(sdk.getWeb3j(), deployer.getCredentials());
        this.mpe = new MultiPartyEscrowContract(sdk.getWeb3j(), MultiPartyEscrow
                .load(configBuilder.getMultiPartyEscrowAddress().get().toString(),
                    sdk.getWeb3j(), transactionManager, gasProvider));
        
        MetadataProvider metadataProvider = sdk.getMetadataProvider(
                IntEnv.TEST_ORG_ID, IntEnv.TEST_SERVICE_ID);
        this.endpointGroup = metadataProvider
            .getServiceMetadata()
            .getEndpointGroupByName(IntEnv.TEST_ENDPOINT_GROUP).get();
        this.paymentGroup = metadataProvider
            .getOrganizationMetadata()
            .getPaymentGroupById(endpointGroup.getPaymentGroupId()).get();
        this.servicePrice = endpointGroup 
            .getPricing().stream()
            .filter(pr -> pr.getPriceModel() == PriceModel.FIXED_PRICE)
            .findFirst().get();
        this.cogsPerCall = servicePrice.getPriceInCogs();
        this.expirationThreshold = paymentGroup.getPaymentDetails()
            .getPaymentExpirationThreshold();
    }

    @After
    public void tearDown() {
        sdk.shutdown();
    }

    @Test
    public void newChannelIsCreatedOnFirstCall() throws Exception {
        run((caller, serviceClient) -> {

            makeServiceCall(serviceClient);

            Stream<PaymentChannel> channels = serviceClient
                .getPaymentChannelProvider()
                .getAllChannels(caller.getAddress());
            assertEquals("Number of payment channels", 1, channels.count());
        });
    }

    @Test
    public void oldChannelIsReusedOnSecondCall() throws Exception {
        run((caller, serviceClient) -> {
            serviceClient.openPaymentChannel(caller, cogsPerCall,
                    expirationThreshold.add(BigInteger.valueOf(1)));

            makeServiceCall(serviceClient);

            Stream<PaymentChannel> channels = serviceClient
                .getPaymentChannelProvider()
                .getAllChannels(caller.getAddress());
            assertEquals("Number of payment channels", 1, channels.count());
        });
    }

    @Test
    public void oldChannelAddFundsOnCall() throws Exception {
        run((caller, serviceClient) -> {
            serviceClient.openPaymentChannel(caller, BigInteger.ZERO,
                    expirationThreshold.add(BigInteger.valueOf(2)));

            makeServiceCall(serviceClient);

            List<PaymentChannel> channels = serviceClient
                .getPaymentChannelProvider()
                .getAllChannels(caller.getAddress())
                .collect(Collectors.toList());
            assertEquals("Number of payment channels", 1, channels.size());
            assertEquals("Payment channel balance",
                    cogsPerCall, channels.get(0).getValue());
        });
    }

    @Test
    public void oldChannelIsExtendedOnCall() throws Exception {
        run((caller, serviceClient) -> {
            PaymentChannel channel = serviceClient.openPaymentChannel(
                    caller, cogsPerCall, BigInteger.ZERO);
            BigInteger blockBeforeCall = Utils.wrapExceptions(() -> sdk.getWeb3j().ethBlockNumber().send().getBlockNumber());

            makeServiceCall(serviceClient);

            List<PaymentChannel> channels = serviceClient
                .getPaymentChannelProvider()
                .getAllChannels(caller.getAddress())
                .collect(Collectors.toList());
            assertEquals("Number of payment channels", 1, channels.size());
            assertEquals("Payment channel expiration block",
                    blockBeforeCall.add(expirationThreshold.add(BigInteger.valueOf(2))),
                    channels.get(0).getExpiration());
        });
    }

    @Test
    public void oldChannelIsExtendedAndFundsAddedOnCall() throws Exception {
        run((caller, serviceClient) -> {
            PaymentChannel channel = serviceClient.openPaymentChannel(
                    caller, BigInteger.ZERO, BigInteger.ZERO);
            BigInteger blockBeforeCall = Utils.wrapExceptions(() -> sdk.getWeb3j().ethBlockNumber().send().getBlockNumber());

            makeServiceCall(serviceClient);

            List<PaymentChannel> channels = serviceClient
                .getPaymentChannelProvider()
                .getAllChannels(caller.getAddress())
                .collect(Collectors.toList());
            assertEquals("Number of payment channels", 1, channels.size());
            assertEquals("Payment channel expiration block",
                    blockBeforeCall.add(expirationThreshold.add(BigInteger.valueOf(2))),
                    channels.get(0).getExpiration());
            assertEquals("Payment channel balance",
                    cogsPerCall, channels.get(0).getValue());
        });
    }

    private void makeServiceCall(ServiceClient serviceClient) {
        CalculatorBlockingStub stub = serviceClient.getGrpcStub(CalculatorGrpc::newBlockingStub);

        Numbers numbers = Numbers.newBuilder()
            .setA(7)
            .setB(6)
            .build();
        Result result = stub.mul(numbers);

        assertEquals("Result of 6 * 7", Result.newBuilder().setValue(42).build(), result);
    }


    private void run(BiConsumer<Identity, ServiceClient> test) throws Exception {
        PrivateKeyIdentity caller = setupNewIdentity();

        StaticConfiguration config = configBuilder
            .setSignerType(Configuration.IdentityType.PRIVATE_KEY)
            .setSignerPrivateKey(caller.getCredentials().getEcKeyPair().getPrivateKey().toByteArray())
            .build();
        
        Sdk sdk = new Sdk(config);
        try {

            PaymentStrategy paymentStrategy = new OnDemandPaymentChannelPaymentStrategy(sdk);
            ServiceClient serviceClient = sdk.newServiceClient(IntEnv.TEST_ORG_ID,
                    IntEnv.TEST_SERVICE_ID, endpointGroup.getGroupName(), paymentStrategy); 
            try {
                
                test.accept(caller, serviceClient);

            } finally {
                serviceClient.shutdownNow();
            }

        } finally {
            sdk.shutdown();
        }
    }

    private PrivateKeyIdentity setupNewIdentity() throws Exception {
        PrivateKeyIdentity identity = new MnemonicIdentity("random mnemonic #" + Math.random(), 0);

        Transfer.sendFunds(sdk.getWeb3j(), deployer.getCredentials(), identity.getAddress().toString(),
                BigDecimal.valueOf(1.0), Convert.Unit.ETHER).send();
        // TODO: implement functions to convert cogs and AGIs
        mpe.transfer(identity.getAddress(), BigInteger.valueOf(1000000));

        return identity;
    }

}
