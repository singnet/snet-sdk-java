package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.math.BigInteger;

import io.singularitynet.sdk.registry.*;
import io.singularitynet.sdk.client.*;
import io.singularitynet.sdk.test.TestServiceGrpc.TestServiceBlockingStub;

public class SingleServiceSingleClientTest {

    private RegistryMock registry;
    private IpfsMock ipfs;

    private TestServer server;

    private String orgId;
    private String serviceId;
    private Pricing pricing;
    private ServiceMetadata service;
    private ServiceRegistration serviceRegistration;

    private ServiceClient serviceClient;
    private TestServiceBlockingStub serviceStub;

    @Before
    public void setUp() {
        registry = new RegistryMock();
        ipfs = new IpfsMock();

        server = TestServer.start();

        orgId = "test-org-id";

        serviceId = "test-service-id";
        pricing = Pricing.newBuilder()
            .setPriceModel(PriceModel.FIXED_PRICE)
            .setPriceInCogs(BigInteger.valueOf(7))
            .build();
        service = ServiceMetadata.newBuilder()
            .setDisplayName("Test Service Name")
            .setMpeAddress("0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C")
            .addEndpointGroup(EndpointGroup.newBuilder()
                    .setGroupName("default_group")
                    .addPricing(pricing)
                    .addEndpoint(server.getEndpoint())
                    .setPaymentGroupId("m5FKWq4hW0foGW5qSbzGSjgZRuKs7A1ZwbIrJ9e96rc=")
                    .build())
            .build();
        URI metadataUri = ipfs.addService(service);
        serviceRegistration = ServiceRegistration.newBuilder()
            .setServiceId(serviceId)
            .setMetadataUri(metadataUri)
            .build();
        registry.addServiceRegistration(orgId, serviceId, serviceRegistration);

        RegistryContract registryContract = new RegistryContract(registry.get());
        MetadataStorage metadataStorage = new IpfsMetadataStorage(ipfs.get());
        MetadataProvider metadataProvider = new RegistryMetadataProvider(
                orgId, serviceId, registryContract, metadataStorage);
        serviceClient = new BaseServiceClient(metadataProvider); 

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

        assertEquals(Output.newBuilder().setOutput("ping").build(), output);
    }

}
