package io.singularitynet.sdk.payment;

import io.grpc.Metadata;

public interface Payment {

    static final Metadata.Key<String> SNET_PAYMENT_TYPE = Metadata.Key.of("snet-payment-type", Metadata.ASCII_STRING_MARSHALLER);

    void toMetadata(Metadata headers);

}
