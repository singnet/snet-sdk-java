package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.math.BigInteger;
import java.util.Optional;
import org.web3j.protocol.core.Ethereum;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import static io.singularitynet.sdk.common.Utils.base64ToBytes;
import io.singularitynet.sdk.registry.*;
import io.singularitynet.sdk.client.*;
import io.singularitynet.sdk.mpe.*;
import io.singularitynet.sdk.ethereum.*;
import io.singularitynet.sdk.daemon.*;
import io.singularitynet.sdk.test.TestServiceGrpc.TestServiceBlockingStub;

public class SingleServiceSingleClientTest {

    private Ethereum ethereum;
    private RegistryMock registry;
    private IpfsMock ipfs;
    private MultiPartyEscrowMock mpe;

    private TestServer server;

    private String orgId;
    private OrganizationMetadata org;
    private OrganizationRegistration orgRegistration;
    private String serviceId;
    private Pricing pricing;
    private ServiceMetadata service;
    private ServiceRegistration serviceRegistration;
    private Signer signer;
    private BigInteger channelId;
    private PaymentChannel paymentChannel;
    private Payment expectedPayment;

    private ServiceClient serviceClient;
    private TestServiceBlockingStub serviceStub;

    @Before
    public void setUp() throws Exception {
        ethereum = mock(Ethereum.class);
        registry = new RegistryMock();
        ipfs = new IpfsMock();
        mpe = new MultiPartyEscrowMock();

        server = TestServer.start();

        orgId = "test-org-id";
        org = OrganizationMetadata.newBuilder()
            .setOrgName("Test Organization")
            .setOrgId(orgId)
            .addPaymentGroup(PaymentGroup.newBuilder()
                    .setGroupName("default_group")
                    .setPaymentGroupId(base64ToBytes("m5FKWq4hW0foGW5qSbzGSjgZRuKs7A1ZwbIrJ9e96rc="))
                    .setPaymentDetails(PaymentDetails.newBuilder()
                        .setPaymentAddress("0xfA8a01E837c30a3DA3Ea862e6dB5C6232C9b800A")
                        .setPaymentExpirationThreshold(BigInteger.valueOf(100))
                        .build())
                    .build())
            .build();
        URI orgMetadataUri = ipfs.addOrganization(org);
        orgRegistration = OrganizationRegistration.newBuilder()
            .setOrgId(orgId)
            .setMetadataUri(orgMetadataUri)
            .addServiceId(serviceId)
            .build();
        registry.addOrganizationRegistration(orgId, orgRegistration);

        serviceId = "test-service-id";
        pricing = Pricing.newBuilder()
            .setPriceModel(PriceModel.FIXED_PRICE)
            .setPriceInCogs(BigInteger.valueOf(11))
            .build();
        String endpointGroupName = "default_group";
        service = ServiceMetadata.newBuilder()
            .setDisplayName("Test Service Name")
            .setMpeAddress("0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C")
            .addEndpointGroup(EndpointGroup.newBuilder()
                    .setGroupName(endpointGroupName)
                    .addPricing(pricing)
                    .addEndpoint(server.getEndpoint())
                    .setPaymentGroupId(base64ToBytes("m5FKWq4hW0foGW5qSbzGSjgZRuKs7A1ZwbIrJ9e96rc="))
                    .build())
            .build();
        URI serviceMetadataUri = ipfs.addService(service);
        serviceRegistration = ServiceRegistration.newBuilder()
            .setServiceId(serviceId)
            .setMetadataUri(serviceMetadataUri)
            .build();
        registry.addServiceRegistration(orgId, serviceId, serviceRegistration);

        signer = new PrivateKeyIdentity(base64ToBytes("1PeCDRD7vLjqiGoHl7A+yPuJIy8TdbNc1vxOyuPjxBM="));
        channelId = BigInteger.valueOf((long)(Math.random() * 100));
        paymentChannel = PaymentChannel.newBuilder()
            .setChannelId(channelId)
            .setMpeContractAddress("0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C")
            .setNonce(BigInteger.valueOf(7))
            .setSender("0xC4f3BFE7D69461B7f363509393D44357c084404c")
            .setSigner(signer.getAddress())
            .setRecipient("0xfA8a01E837c30a3DA3Ea862e6dB5C6232C9b800A")
            .setPaymentGroupId(base64ToBytes("m5FKWq4hW0foGW5qSbzGSjgZRuKs7A1ZwbIrJ9e96rc="))
            .setValue(BigInteger.valueOf(41))
            .setExpiration(BigInteger.valueOf(125))
            .setSpentAmount(BigInteger.valueOf(0))
            .build();
        mpe.setContractAddress("0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C");
        mpe.addPaymentChannel(paymentChannel);
        expectedPayment = EscrowPayment.newBuilder()
            .setPaymentChannel(paymentChannel)
            .setAmount(BigInteger.valueOf(11))
            .setSigner(signer)
            .build();

        BigInteger curEthBlock = BigInteger.valueOf(53);
        EthBlockNumber ethBlockNumber = mock(EthBlockNumber.class);
        when(ethBlockNumber.getBlockNumber()).thenReturn(curEthBlock);
        Request ethBlockNumberReq = mock(Request.class);
        when(ethBlockNumberReq.send()).thenReturn(ethBlockNumber);
        when(ethereum.ethBlockNumber()).thenReturn(ethBlockNumberReq);
        server.getDaemon().setChannelStateIsAbsent(paymentChannel);

        RegistryContract registryContract = new RegistryContract(registry.get());
        MetadataStorage metadataStorage = new IpfsMetadataStorage(ipfs.get());
        MetadataProvider metadataProvider = new RegistryMetadataProvider(
                orgId, serviceId, registryContract, metadataStorage);
        MultiPartyEscrowContract mpeContract = new MultiPartyEscrowContract(mpe.get());
        DaemonConnection connection = new FirstEndpointDaemonConnection(endpointGroupName,
                metadataProvider);
        PaymentChannelStateService stateService = new PaymentChannelStateService(
                connection, ethereum, signer);
        PaymentChannelProvider paymentChannelProvider =
            new ContractPaymentChannelProvider(mpeContract, stateService);
        PaymentStrategy paymentStrategy = new FixedPaymentChannelPaymentStrategy(channelId, signer);
        serviceClient = new BaseServiceClient(connection, metadataProvider,
                paymentChannelProvider, paymentStrategy); 

        serviceStub = serviceClient.getGrpcStub(TestServiceGrpc::newBlockingStub);
    }

    @After
    public void tearDown() {
        serviceClient.shutdownNow();
        server.shutdownNow();
    }

    @Test
    public void clientCanCallGrpcServiceUsingSnetSdkGrpcChannel() {
        Output output = serviceStub.echo(Input.newBuilder().setInput("ping").build());

        assertEquals("Result returned", Output.newBuilder().setOutput("ping").build(), output);
    }

    @Test
    public void clientSendsPaymentDataInGrpcMetadata() {
        Output output = serviceStub.echo(Input.newBuilder().setInput("ping").build());

        assertEquals("Number of payments received by daemon", 1, server.getDaemon().getPayments().size());
    }

    @Test
    public void sendPaymentForChannelNotUsedBefore() {
        Output output = serviceStub.echo(Input.newBuilder().setInput("ping").build());

        assertEquals("Payment received by daemon", expectedPayment, server.getDaemon().getPayments().get(0));
    }

    @Test
    public void sendPaymentForChannelUsedBeforeNoClaim() {
        expectedPayment = EscrowPayment.newBuilder()
            .setPaymentChannel(paymentChannel)
            .setAmount(BigInteger.valueOf(14))
            .setSigner(signer)
            .build();
        EscrowPayment prevPayment = EscrowPayment.newBuilder()
            .setPaymentChannel(paymentChannel)
            .setAmount(BigInteger.valueOf(3))
            .setSigner(signer)
            .build();
        server.getDaemon().setChannelState(channelId,
                PaymentChannelStateReply.newBuilder()
                .setCurrentNonce(prevPayment.getChannelNonce())
                .setCurrentSignedAmount(prevPayment.getAmount())
                .setCurrentSignature(prevPayment.getSignature())
                .build());

        Output output = serviceStub.echo(Input.newBuilder().setInput("ping").build());

        assertEquals("Payment received by daemon", expectedPayment, server.getDaemon().getPayments().get(0));
    }

}
