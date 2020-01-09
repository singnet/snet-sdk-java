package com.example.snetdemo;
import java.io.Closeable;
import java.math.BigInteger;

import io.singularitynet.sdk.client.FixedPaymentChannelPaymentStrategy;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.service.minecraftizing.MinecraftizingServiceGrpc;

public class MinecraftizingService implements Closeable
{
    private final ServiceClient serviceClient;
    private final MinecraftizingServiceGrpc.MinecraftizingServiceBlockingStub stub;

    MinecraftizingService(SnetSdk sdk, BigInteger channelId) {

        PaymentStrategy paymentStrategy = new FixedPaymentChannelPaymentStrategy(channelId);
        serviceClient = sdk.getSdk().newServiceClient("snet", "minecraftizing-service",
                "default_group", paymentStrategy);
        stub = serviceClient.getGrpcStub(MinecraftizingServiceGrpc::newBlockingStub);
    }

    MinecraftizingServiceGrpc.MinecraftizingServiceBlockingStub getStub() {
        return stub;
    }

    @Override
    public void close() {
        serviceClient.shutdownNow();
    }

}
