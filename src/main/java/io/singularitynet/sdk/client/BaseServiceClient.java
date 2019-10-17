package io.singularitynet.sdk.client;

import io.grpc.*;
import java.util.function.Function;
import com.google.gson.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.net.URL;

import io.singularitynet.sdk.registry.*;

public class BaseServiceClient implements ServiceClient {

    private final ServiceMetadata serviceMetadata;
    private ManagedChannel channel;

    public BaseServiceClient(String orgId, String serviceId, RegistryContract registryContract, MetadataStorage metadataStorage) {
        ServiceRegistration registration = registryContract.getServiceRegistrationById(orgId, serviceId).get();
        byte[] metadataBytes = metadataStorage.get(registration.getMetadataUri());
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        serviceMetadata = gson.fromJson(new String(metadataBytes, UTF_8), ServiceMetadata.class);
    }

    @Override
    public <T> T getGrpcStub(Function<Channel, T> constructor) {
        URL url = serviceMetadata.getEndpointGroups().get(0).getEndpoints().get(0);
        channel = ManagedChannelBuilder
            .forAddress(url.getHost(), url.getPort())
            .usePlaintext()
            .build();
        return constructor.apply(channel);
    }

    @Override
    public void shutdownNow() {
        channel.shutdownNow();
    }

}
