package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;

import java.math.BigInteger;

import io.singularitynet.sdk.registry.*;
import io.singularitynet.sdk.mpe.*;
import io.singularitynet.sdk.client.*;
import io.singularitynet.sdk.ethereum.*;
import io.singularitynet.sdk.daemon.*;
import io.singularitynet.sdk.test.TestServiceGrpc.TestServiceBlockingStub;

public class SingleServiceSingleClientTest {

    private Environment env;

    private BigInteger price;
    private PaymentChannel paymentChannel;

    private Sdk sdk;
    private ServiceClient serviceClient;
    private TestServiceBlockingStub serviceStub;

    @Before
    public void setUp() throws Exception {
        env = Environment.env();

        String orgId = "test-org-id";
        env.newOrganizationMetadata(orgId);
        env.registerOrganization(orgId);

        String serviceId = "test-service-id";
        ServiceMetadata.Builder service = env.newServiceMetadata(serviceId, orgId);
        price = BigInteger.valueOf(11);
        EndpointGroup endpointGroup = env.newEndpointGroup(orgId)
            .clearPricing()
            .addPricing(env.newPricing().setPriceInCogs(price).build())
            .build();
        service.clearEndpointGroups().addEndpointGroup(endpointGroup);
        env.registerService(orgId, serviceId);

        Signer signer = env.newSigner();

        paymentChannel = env.newPaymentChannel(endpointGroup.getPaymentGroupId(), signer).build();
        env.daemon().setChannelStateIsAbsent(paymentChannel);

        env.updateMocks();

        sdk = new Sdk(env.web3j(), env.ipfs().get(), signer,
                env.registry().get(), env.mpe().get());

        PaymentStrategy paymentStrategy = new FixedPaymentChannelPaymentStrategy(
                paymentChannel.getChannelId());
        serviceClient = sdk.newServiceClient(orgId, serviceId,
                endpointGroup.getGroupName(), paymentStrategy);

        serviceStub = serviceClient.getGrpcStub(TestServiceGrpc::newBlockingStub);
    }

    @After
    public void tearDown() {
        sdk.shutdown();
        serviceClient.shutdownNow();
        env.server().shutdownNow();
    }

    @Test
    public void clientCanCallGrpcServiceUsingSnetSdkGrpcChannel() {
        Output output = serviceStub.echo(Input.newBuilder().setInput("ping").build());

        assertEquals("Result returned", Output.newBuilder().setOutput("ping").build(), output);
    }

    @Test
    public void clientSendsPaymentDataInGrpcMetadata() {
        Output output = serviceStub.echo(Input.newBuilder().setInput("ping").build());

        assertEquals("Number of payments received by daemon", 1, env.daemon().getPayments().size());
    }

    @Test
    public void sendPaymentForChannelNotUsedBefore() {
        Output output = serviceStub.echo(Input.newBuilder().setInput("ping").build());

        EscrowPayment expectedPayment = env.newEscrowPayment(paymentChannel).setAmount(price).build();
        assertEquals("Payment received by daemon", expectedPayment, env.daemon().getPayments().get(0));
    }

    @Test
    public void sendPaymentForChannelUsedBeforeNoClaim() {
        BigInteger prevPrice = BigInteger.valueOf(3);
        EscrowPayment prevPayment = env.newEscrowPayment(paymentChannel)
            .setAmount(prevPrice).build();
        env.daemon().setChannelState(paymentChannel.getChannelId(),
                env.newPaymentChannelStateReply(prevPayment).build());

        Output output = serviceStub.echo(Input.newBuilder().setInput("ping").build());

        EscrowPayment expectedPayment = env.newEscrowPayment(paymentChannel)
            .setAmount(prevPrice.add(price)).build();
        assertEquals("Payment received by daemon", expectedPayment, env.daemon().getPayments().get(0));
    }

}
