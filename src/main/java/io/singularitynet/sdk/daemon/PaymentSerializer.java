package io.singularitynet.sdk.daemon;

import io.grpc.Metadata;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.math.BigInteger;
import java.util.function.Function;

public class PaymentSerializer {

    private static final Map<String, Function<Metadata, Payment>> readerByType = new HashMap<>();

    public static void register(String type, Function<Metadata, Payment> reader) {
        readerByType.put(type, reader);
    }

    public static Optional<Payment> fromMetadata(Metadata headers) {
        if (!headers.containsKey(Payment.SNET_PAYMENT_TYPE)) {
            return Optional.empty();
        }

        String paymentType = headers.get(Payment.SNET_PAYMENT_TYPE);
        Function<Metadata, Payment> reader = readerByType.get(paymentType);

        if (reader == null) {
            throw new IllegalArgumentException("Unexpected payment type: " + paymentType);
        }

        return Optional.of(reader.apply(headers)); 
    }

    public static void toMetadata(Payment payment, Metadata headers) {
        payment.toMetadata(headers);
    }

    public static final Metadata.AsciiMarshaller<BigInteger> ASCII_BIGINTEGER_MARSHALLER =
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
