package io.singularitynet.sdk.mpe;

import io.grpc.Metadata;
import java.util.Optional;
import java.math.BigInteger;

public class PaymentSerializer {

    public static Optional<Payment> fromMetadata(Metadata headers) {
        if (!headers.containsKey(Payment.SNET_PAYMENT_TYPE)) {
            return Optional.empty();
        }

        String paymentType = headers.get(Payment.SNET_PAYMENT_TYPE);
        if (paymentType.equals(EscrowPayment.PAYMENT_TYPE_ESCROW)) {
            return Optional.of(EscrowPayment.fromMetadata(headers)); 
        } else {
            throw new IllegalArgumentException("Unexpected payment type: " + paymentType);
        }
    }

    public static void toMetadata(Payment payment, Metadata headers) {
        payment.toMetadata(headers);
    }

    static final Metadata.AsciiMarshaller<BigInteger> ASCII_BIGINTEGER_MARSHALLER =
        new Metadata.AsciiMarshaller<BigInteger>() {

            @Override
            public BigInteger parseAsciiString(String serialized) {
                return new BigInteger(serialized);
            }

            @Override
            public String toAsciiString(BigInteger value) {
                return value.toString();
            }

        };

}
