package io.singularitynet.sdk.client;

import io.grpc.*;
import java.util.function.Function;
import java.net.URL;

import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.ServiceMetadata;

public class BaseServiceClient implements ServiceClient {

    private final MetadataProvider metadataProvider;
    private ManagedChannel channel;

    public BaseServiceClient(MetadataProvider metadataProvider) {
        this.metadataProvider = metadataProvider;
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
        ServiceMetadata serviceMetadata = metadataProvider.getServiceMetadata();
        URL url = serviceMetadata.getEndpointGroups().get(0).getEndpoints().get(0);
        return ManagedChannelBuilder
            .forAddress(url.getHost(), url.getPort())
            .usePlaintext()
            .build();
    }

}
