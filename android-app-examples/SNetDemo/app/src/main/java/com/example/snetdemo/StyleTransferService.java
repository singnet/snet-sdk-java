package com.example.snetdemo;

import java.io.Closeable;
import java.math.BigInteger;

import io.singularitynet.sdk.client.FixedPaymentChannelPaymentStrategy;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.service.styletransfer.StyleTransferGrpc;

public class StyleTransferService implements Closeable
{
    private final ServiceClient serviceClient;
    private final StyleTransferGrpc.StyleTransferBlockingStub stub;

    StyleTransferService(SnetSdk sdk, BigInteger channelId) {

        PaymentStrategy paymentStrategy = new FixedPaymentChannelPaymentStrategy(channelId);
        serviceClient = sdk.getSdk().newServiceClient("snet", "style-transfer",
                "default_group", paymentStrategy);
        stub = serviceClient.getGrpcStub(StyleTransferGrpc::newBlockingStub);
    }

    StyleTransferGrpc.StyleTransferBlockingStub getStub() {
        return stub;
    }

    @Override
    public void close() {
        serviceClient.shutdownNow();
    }

}
