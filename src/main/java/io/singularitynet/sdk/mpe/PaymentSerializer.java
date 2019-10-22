package io.singularitynet.sdk.mpe;

import io.grpc.Metadata;
import java.util.Optional;

public class PaymentSerializer {

    private static final Metadata.Key<String> SNET_PAYMENT_TYPE = Metadata.Key.of("snet-payment-type", Metadata.ASCII_STRING_MARSHALLER);

    private static final String PAYMENT_TYPE_ESCROW = "escrow";

    public static Optional<Payment> fromMetadata(Metadata headers) {
        if (!headers.containsKey(SNET_PAYMENT_TYPE)) {
            return Optional.empty();
        }

        String paymentType = headers.get(SNET_PAYMENT_TYPE);
        if (paymentType.equals(PAYMENT_TYPE_ESCROW)) {
            return Optional.of(EscrowPayment.fromMetadata(headers)); 
        } else {
            throw new IllegalArgumentException("Unexpected payment type: " + paymentType);
        }
    }

    public static void toMetadata(Payment payment, Metadata headers) {
        if (payment instanceof EscrowPayment) {
            headers.put(SNET_PAYMENT_TYPE, PAYMENT_TYPE_ESCROW);
            payment.toMetadata(headers);
        } else {
            throw new IllegalArgumentException("Unexpected payment class: " + payment.getClass());
        }
    }

}
