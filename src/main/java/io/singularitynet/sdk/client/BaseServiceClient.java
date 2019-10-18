package io.singularitynet.sdk.client;

import io.grpc.*;
import java.util.function.Function;
import com.google.gson.*;
import java.net.URL;

import io.singularitynet.sdk.registry.*;
import static io.singularitynet.sdk.registry.Utils.*;

public class BaseServiceClient implements ServiceClient {

    private final String orgId;
    private final String serviceId;
    private final RegistryContract registryContract;
    private final MetadataStorage metadataStorage;
    private ManagedChannel channel;

    public BaseServiceClient(String orgId, String serviceId, RegistryContract registryContract, MetadataStorage metadataStorage) {
        this.orgId = orgId;
        this.serviceId = serviceId;
        this.metadataStorage = metadataStorage;
        this.registryContract = registryContract;
    }

    @Override
    public <T> T getGrpcStub(Function<Channel, T> constructor) {
        return constructor.apply(getChannelLazy());
    }

    @Override
    public void shutdownNow() {
        channel.shutdownNow();
    }

    private ManagedChannel getChannelLazy() {
        if (channel == null) {
            channel = getChannel();
        }
        return channel;
    }

    private ManagedChannel getChannel() {
        ServiceRegistration registration = registryContract.getServiceRegistrationById(orgId, serviceId).get();
        byte[] metadataBytes = metadataStorage.get(registration.getMetadataUri());
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        ServiceMetadata serviceMetadata = gson.fromJson(bytesToStr(metadataBytes), ServiceMetadata.class);
        URL url = serviceMetadata.getEndpointGroups().get(0).getEndpoints().get(0);
        return ManagedChannelBuilder
            .forAddress(url.getHost(), url.getPort())
            .usePlaintext()
            .build();
    }

}
