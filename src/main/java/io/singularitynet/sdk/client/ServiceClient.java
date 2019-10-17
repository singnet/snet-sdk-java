package io.singularitynet.sdk.client;

import io.grpc.Channel;
import java.util.function.Function;

public interface ServiceClient {

    <T> T getGrpcStub(Function<Channel, T> constructor);
    void shutdownNow();

}
