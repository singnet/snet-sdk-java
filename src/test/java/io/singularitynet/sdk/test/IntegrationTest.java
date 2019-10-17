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
    private Config config;

    private ServiceClient client;

    @Before
    public void setUp() {
        testServer = TestService.start(TestService.RANDOM_AVAILABLE_PORT);
        registry = new RegistryMock();
        ipfs = new IpfsMock();
        config = new Config() {
            public Registry getRegistry() { return registry.getRegistry(); }
            public IPFS getIpfs() { return ipfs.getIpfs(); }
        };
        registry.getServiceRegistrationById("test-org-id", "test-service-id")
            .returns(serviceRegistration()
                    .setId("test-service-id")
                    .setMetadataUri("ipfs://QmR3anSdm4s13iLt3zzyrSbtvCDJNwhkrYG6yFGFHXBznb"));
        ipfs.cat("QmR3anSdm4s13iLt3zzyrSbtvCDJNwhkrYG6yFGFHXBznb")
            .returns(serviceMetadataJson(testServer.getPort()));
        client = new BaseServiceClient("test-org-id", "test-service-id", config); 
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
