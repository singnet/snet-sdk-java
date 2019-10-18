package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.grpc.Server;
import io.ipfs.api.IPFS;

import io.singularitynet.sdk.contracts.*;
import io.singularitynet.sdk.client.*;
import io.singularitynet.sdk.registry.*;
import io.singularitynet.sdk.test.TestServiceGrpc.TestServiceBlockingStub;
import static io.singularitynet.sdk.registry.Utils.*;
import static io.singularitynet.sdk.registry.RegistryMock.*;
import static io.singularitynet.sdk.registry.IpfsMock.*;

public class IntegrationTest {

    private Server testServer;
    private RegistryMock registry;
    private IpfsMock ipfs;

    private ServiceClient client;

    @Before
    public void setUp() {
        testServer = TestService.start(TestService.RANDOM_AVAILABLE_PORT);
        registry = new RegistryMock();
        ipfs = new IpfsMock();
        registry.getServiceRegistrationById("test-org-id", "test-service-id")
            .returns(serviceRegistration()
                    .setId("test-service-id")
                    .setMetadataUri("ipfs://QmR3anSdm4s13iLt3zzyrSbtvCDJNwhkrYG6yFGFHXBznb"));
        ipfs.cat("QmR3anSdm4s13iLt3zzyrSbtvCDJNwhkrYG6yFGFHXBznb")
            .returns(serviceMetadataJson(testServer.getPort()));
        RegistryContract registryContract = new RegistryContract(registry.get());
        MetadataStorage metadataStorage = new IpfsMetadataStorage(ipfs.get());
        client = new BaseServiceClient("test-org-id", "test-service-id",
                registryContract, metadataStorage); 
    }

    @After
    public void tearDown() {
        client.shutdownNow();
        testServer.shutdownNow();
    }

    @Test
    public void clientCanCallGrpcServiceUsingSnetSdkGrpcChannel() {
        TestServiceBlockingStub stub = client.getGrpcStub(TestServiceGrpc::newBlockingStub);

        Output output = stub.echo(Input.newBuilder().setInput("ping").build());

        assertEquals(Output.newBuilder().setOutput("ping").build(), output);
    }

}
