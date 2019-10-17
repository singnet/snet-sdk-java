package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.grpc.Server;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import javax.json.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import io.singularitynet.sdk.client.*;
import io.singularitynet.sdk.registry.*;
import io.singularitynet.sdk.test.TestServiceGrpc.TestServiceBlockingStub;

public class IntegrationTest {

    private Server testServer;
    private RegistryContract registryContract;
    private MetadataStorage metadataStorage;

    private ServiceClient client;

    @Before
    public void setUp() throws URISyntaxException {
        testServer = TestService.start(TestService.RANDOM_AVAILABLE_PORT);

        registryContract = mock(RegistryContract.class);
        when(registryContract.getServiceRegistrationById(eq("test-org-id"), eq("test-service-id")))
            .thenReturn(Optional.of(ServiceRegistration.newBuilder()
                    .setServiceId("test-service-id")
                    .setMetadataUri(new URI("ipfs://test-metadata-uri"))
                    .build()));

        metadataStorage = mock(MetadataStorage.class);
        when(metadataStorage.get(eq(new URI("ipfs://test-metadata-uri")))).thenReturn(
            Json.createObjectBuilder()
                .add("version", "1")
                .add("display_name", "Test Service Name")
                .add("encoding", "proto")
                .add("service_type", "grpc")
                .add("model_ipfs_hash", "QmR3anSdm4s13iLt3zzyrSbtvCDJNwhkrYG6yFGFHXBznb")
                .add("mpe_address", "0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C")
                .add("groups", Json.createArrayBuilder()
                    .add(Json.createObjectBuilder()
                        .add("group_name", "default_group")
                        .add("pricing", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                .add("price_model", "fixed_price")
                                .add("price_in_cogs", 1)
                                .add("default", true)
                                .build())
                            .build())
                        .add("endpoints", Json.createArrayBuilder()
                            .add("http://localhost:" + testServer.getPort())
                            .build())
                        .add("group_id", "m5FKWq4hW0foGW5qSbzGSjgZRuKs7A1ZwbIrJ9e96rc=")
                        .build())
                    .build())
                .add("assets", Json.createObjectBuilder().build())
                .add("service_description", Json.createObjectBuilder()
                        .add("url", "https://singnet.github.io/dnn-model-services/users_guide/i3d-video-action-recognition.html")
                        .add("description", "This service uses I3D to perform action recognition on videos.")
                        .build())
                .build().toString().getBytes(UTF_8));

        client = new BaseServiceClient("test-org-id", "test-service-id", registryContract, metadataStorage); 
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
