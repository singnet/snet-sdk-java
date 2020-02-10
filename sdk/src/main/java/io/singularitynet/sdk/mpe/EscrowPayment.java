package io.singularitynet.sdk.mpe;

import io.grpc.Metadata;
import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import io.singularitynet.sdk.payment.Payment;
import io.singularitynet.sdk.payment.PaymentSerializer;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.Signature;
import io.singularitynet.sdk.common.Utils;

/**
 * MultiPartyEscrow payment channel payment. It contains information about
 * payment channel and next check signature.
 */
@EqualsAndHashCode
@ToString
public class EscrowPayment implements Payment {

    /**
     * Escrow payment type contant.
     * @see io.singularitynet.sdk.payment.PaymentSerializer#register
     */
    public static final String PAYMENT_TYPE_ESCROW = "escrow";

    static {
        PaymentSerializer.register(PAYMENT_TYPE_ESCROW, EscrowPayment::fromMetadata);
    }

    private static final Metadata.Key<BigInteger> SNET_PAYMENT_CHANNEL_ID =
        Metadata.Key.of("snet-payment-channel-id", PaymentSerializer.ASCII_BIGINTEGER_MARSHALLER);
    private static final Metadata.Key<BigInteger> SNET_PAYMENT_CHANNEL_NONCE =
        Metadata.Key.of("snet-payment-channel-nonce", PaymentSerializer.ASCII_BIGINTEGER_MARSHALLER);
    private static final Metadata.Key<BigInteger> SNET_PAYMENT_CHANNEL_AMOUNT =
        Metadata.Key.of("snet-payment-channel-amount", PaymentSerializer.ASCII_BIGINTEGER_MARSHALLER);
    private static final Metadata.Key<byte[]> SNET_PAYMENT_CHANNEL_SIGNATURE =
        Metadata.Key.of("snet-payment-channel-signature" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private final BigInteger channelId;
    private final BigInteger channelNonce;
    private final BigInteger amount;
    private final Signature signature;

    @Override
    public void toMetadata(Metadata headers) {
        headers.put(PaymentSerializer.SNET_PAYMENT_TYPE, EscrowPayment.PAYMENT_TYPE_ESCROW);
        headers.put(SNET_PAYMENT_CHANNEL_ID, channelId);
        headers.put(SNET_PAYMENT_CHANNEL_NONCE, channelNonce);
        headers.put(SNET_PAYMENT_CHANNEL_AMOUNT, amount);
        headers.put(SNET_PAYMENT_CHANNEL_SIGNATURE, signature.getBytes());
    }

    /**
     * Deserialize escrow payment from metadata.
     * @param headers metadata to get information from.
     * @return escrow payment deserialized.
     */
    public static EscrowPayment fromMetadata(Metadata headers) {
        BigInteger channelId = headers.get(SNET_PAYMENT_CHANNEL_ID);
        BigInteger channelNonce = headers.get(SNET_PAYMENT_CHANNEL_NONCE);
        BigInteger amount = headers.get(SNET_PAYMENT_CHANNEL_AMOUNT);
        byte[] signature = headers.get(SNET_PAYMENT_CHANNEL_SIGNATURE);
        return new EscrowPayment(channelId, channelNonce, amount, new Signature(signature));
    }

    public EscrowPayment(BigInteger channelId, BigInteger channelNonce,
            BigInteger amount, Signature signature) {
        this.channelId = channelId;
        this.channelNonce = channelNonce;
        this.amount = amount;
        this.signature = signature;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public BigInteger getChannelId() {
        return channelId;
    }

    public BigInteger getChannelNonce() {
        return channelNonce;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public Signature getSignature() {
        return signature;
    }

    public static class Builder {
    
        private PaymentChannel paymentChannel;
        private BigInteger amount;
        private Identity signer;
    
        public Builder() {
        }

        public Builder setPaymentChannel(PaymentChannel paymentChannel) {
            this.paymentChannel = paymentChannel;
            return this;
        }

        public Builder setAmount(BigInteger amount) {
            this.amount = amount;
            return this;
        }

        public Builder setSigner(Identity signer) {
            this.signer = signer;
            return this;
        }
    
        public EscrowPayment build() {
            byte[] message = getMessage(paymentChannel, amount);
            Signature signature = signer.sign(message);
            return new EscrowPayment(paymentChannel.getChannelId(),
                    paymentChannel.getNonce(), amount, signature);
        }

    }

    private static final byte[] PAYMENT_MESSAGE_PREFIX = Utils.strToBytes("__MPE_claim_message");

    public static byte[] getMessage(PaymentChannel paymentChannel, BigInteger amount) {
        return Utils.wrapExceptions(() -> {
            ByteArrayOutputStream message = new ByteArrayOutputStream();
            message.write(PAYMENT_MESSAGE_PREFIX);
            message.write(paymentChannel.getMpeContractAddress().toByteArray());
            message.write(Utils.bigIntToBytes32(paymentChannel.getChannelId()));
            message.write(Utils.bigIntToBytes32(paymentChannel.getNonce()));
            message.write(Utils.bigIntToBytes32(amount));
            return message.toByteArray();
        });
    }

}
