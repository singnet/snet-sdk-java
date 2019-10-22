package io.singularitynet.sdk.mpe;

import io.grpc.Metadata;

public class EscrowPayment implements Payment {

    private static final Metadata.Key<String> SNET_PAYMENT_CHANNEL_ID = Metadata.Key.of("snet-payment-channel-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> SNET_PAYMENT_CHANNEL_NONCE = Metadata.Key.of("snet-payment-channel-nonce", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> SNET_PAYMENT_CHANNEL_AMOUNT = Metadata.Key.of("snet-payment-channel-amount", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<byte[]> SNET_PAYMENT_CHANNEL_SIGNATURE = Metadata.Key.of("snet-payment-channel-signature" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    @Override
    public void toMetadata(Metadata headers) {
    }

    public static EscrowPayment fromMetadata(Metadata headers) {
        return new EscrowPayment();
    }

}
