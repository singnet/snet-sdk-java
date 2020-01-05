package com.example.snetdemo;

import java.io.Closeable;
import java.math.BigInteger;

import io.singularitynet.sdk.client.FixedPaymentChannelPaymentStrategy;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.service.semanticsegmentation.SemanticSegmentationGrpc;

public class SemanticSegmentationService implements Closeable
{

    private final ServiceClient serviceClient;
    private final SemanticSegmentationGrpc.SemanticSegmentationBlockingStub stub;

    SemanticSegmentationService(SnetSdk sdk, BigInteger channelId) {
        PaymentStrategy paymentStrategy = new FixedPaymentChannelPaymentStrategy(channelId);
        serviceClient = sdk.getSdk().newServiceClient("snet", "semantic-segmentation",
                "default_group", paymentStrategy);
        stub = serviceClient.getGrpcStub(SemanticSegmentationGrpc::newBlockingStub);
    }

    SemanticSegmentationGrpc.SemanticSegmentationBlockingStub getStub() {
        return stub;
    }

    @Override
    public void close() {
        serviceClient.shutdownNow();
    }}
