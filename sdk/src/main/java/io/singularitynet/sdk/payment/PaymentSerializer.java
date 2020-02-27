package io.singularitynet.sdk.payment;

import io.grpc.Metadata;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.math.BigInteger;
import java.util.function.Function;

/**
 * Utility class which can serialize and deserialize all kinds of payments.
 * Each payment should register its serialization method in this class in order
 * to be deserialized successfully.
 */
public class PaymentSerializer {

    /**
     * gRPC metadata key of SingularityNet payment type.
     */
    public static final Metadata.Key<String> SNET_PAYMENT_TYPE =
        Metadata.Key.of("snet-payment-type", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * gRPC metadata key of the payment signature.
     */
    public static final Metadata.Key<byte[]> SNET_PAYMENT_SIGNATURE =
        Metadata.Key.of("snet-payment-channel-signature" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final Map<String, Function<Metadata, Payment>> readerByType = new HashMap<>();

    private PaymentSerializer() {
    }

    /**
     * Register payment type for serialization.
     * @param type payment type id, this type should be know by daemon (for
     * example "escrow").
     * @param reader function to read payment data from gRPC metadata.
     */
    public static void register(String type, Function<Metadata, Payment> reader) {
        readerByType.put(type, reader);
    }

    /**
     * Deserialize payment from metadata. Method uses SNET_PAYMENT_TYPE field
     * value to find payment deserialization function.
     */
    public static Optional<Payment> fromMetadata(Metadata headers) {
        if (!headers.containsKey(SNET_PAYMENT_TYPE)) {
            return Optional.empty();
        }

        String paymentType = headers.get(SNET_PAYMENT_TYPE);
        Function<Metadata, Payment> reader = readerByType.get(paymentType);

        if (reader == null) {
            throw new IllegalArgumentException("Unexpected payment type: " + paymentType);
        }

        return Optional.of(reader.apply(headers)); 
    }

    /**
     * Serialize payment into metadata.
     * @param payment payment to be serialized.
     * @param headers metadata to keep payment.
     */
    public static void toMetadata(Payment payment, Metadata headers) {
        payment.toMetadata(headers);
    }

    /**
     * java.math.BigInteger gRPC metadata marshaller. Provided to simplify
     * implementing payment serialization methods.
     */
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
