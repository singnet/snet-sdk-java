package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.math.BigInteger;
import java.util.Optional;

import static io.singularitynet.sdk.common.Utils.base64ToBytes;
import io.singularitynet.sdk.registry.*;
import io.singularitynet.sdk.client.*;
import io.singularitynet.sdk.mpe.*;
import io.singularitynet.sdk.ethereum.*;
import io.singularitynet.sdk.test.TestServiceGrpc.TestServiceBlockingStub;

public class SingleServiceSingleClientTest {

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
    public void setUp() {
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
        service = ServiceMetadata.newBuilder()
            .setDisplayName("Test Service Name")
            .setMpeAddress("0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C")
            .addEndpointGroup(EndpointGroup.newBuilder()
                    .setGroupName("default_group")
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
            .setSigner("0x46EF7d49aaA68B29C227442BDbD18356415f8304")
            .setRecipient("0xfA8a01E837c30a3DA3Ea862e6dB5C6232C9b800A")
            .setPaymentGroupId(base64ToBytes("m5FKWq4hW0foGW5qSbzGSjgZRuKs7A1ZwbIrJ9e96rc="))
            .setValue(BigInteger.valueOf(41))
            .setExpiration(BigInteger.valueOf(125))
            .build();
        mpe.setContractAddress("0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C");
        mpe.addPaymentChannel(paymentChannel);
        expectedPayment = EscrowPayment.newBuilder()
            .setPaymentChannel(paymentChannel)
            .setAmount(BigInteger.valueOf(11))
            .setSigner(signer)
            .build();

        RegistryContract registryContract = new RegistryContract(registry.get());
        MetadataStorage metadataStorage = new IpfsMetadataStorage(ipfs.get());
        MetadataProvider metadataProvider = new RegistryMetadataProvider(
                orgId, serviceId, registryContract, metadataStorage);
        MultiPartyEscrowContract mpeContract = new MultiPartyEscrowContract(mpe.get());
        PaymentChannelProvider paymentChannelProvider = new ContractPaymentChannelProvider(mpeContract);
        serviceClient = new BaseServiceClient(metadataProvider,
                new FixedPaymentChannelPaymentStrategy(channelId, signer),
                paymentChannelProvider); 

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

}
