package io.singularitynet.sdk.client;

import io.grpc.Channel;
import java.util.function.Function;

import io.singularitynet.sdk.mpe.PaymentChannelProvider;
import io.singularitynet.sdk.registry.MetadataProvider;

public interface ServiceClient {

    <T> T getGrpcStub(Function<Channel, T> constructor);
    void shutdownNow();

    PaymentChannelProvider getPaymentChannelProvider();
    MetadataProvider getMetadataProvider();

}
