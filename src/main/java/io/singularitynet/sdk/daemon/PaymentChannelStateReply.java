package io.singularitynet.sdk.daemon;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigInteger;

@EqualsAndHashCode
@ToString
public class PaymentChannelStateReply {

    private final BigInteger currentNonce;
    private final BigInteger currentSignedAmount;
    // TODO: replace by Signature class to implement toString()
    private final byte[] currentSignature;
    private final BigInteger oldNonceSignedAmount;
    private final byte[] oldNonceSignature;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private PaymentChannelStateReply(Builder builder) {
        this.currentNonce = builder.currentNonce;
        this.currentSignedAmount = builder.currentSignedAmount;
        this.currentSignature = builder.currentSignature;
        this.oldNonceSignedAmount = builder.oldNonceSignedAmount;
        this.oldNonceSignature = builder.oldNonceSignature;
    }

    public BigInteger getCurrentNonce() {
        return currentNonce;
    }

    public BigInteger getCurrentSignedAmount() {
        return currentSignedAmount;
    }

    public boolean hasCurrentSignedAmount() {
        return currentSignedAmount != null;
    }

    public byte[] getCurrentSignature() {
        return currentSignature;
    }

    public BigInteger getOldNonceSignedAmount() {
        return oldNonceSignedAmount;
    }

    public byte[] getOldNonceSignature() {
        return oldNonceSignature;
    }

    public static class Builder {

        private BigInteger currentNonce;
        private BigInteger currentSignedAmount;
        private byte[] currentSignature;
        private BigInteger oldNonceSignedAmount;
        private byte[] oldNonceSignature;

        private Builder() {
        }

        private Builder(PaymentChannelStateReply object) {
            this.currentNonce = object.currentNonce;
            this.currentSignedAmount = object.currentSignedAmount;
            this.currentSignature = object.currentSignature;
            this.oldNonceSignedAmount = object.oldNonceSignedAmount;
            this.oldNonceSignature = object.oldNonceSignature;
        }

        public Builder setCurrentNonce(BigInteger currentNonce) {
            this.currentNonce = currentNonce;
            return this;
        }

        public Builder setCurrentSignedAmount(BigInteger currentSignedAmount) {
            this.currentSignedAmount = currentSignedAmount;
            return this;
        }

        public Builder setCurrentSignature(byte[] currentSignature) {
            this.currentSignature = currentSignature;
            return this;
        }

        public Builder setOldNonceSignedAmount(BigInteger oldNonceSignedAmount) {
            this.oldNonceSignedAmount = oldNonceSignedAmount;
            return this;
        }

        public Builder setOldNonceSignature(byte[] oldNonceSignature) {
            this.oldNonceSignature = oldNonceSignature;
            return this;
        }

        public PaymentChannelStateReply build() {
            return new PaymentChannelStateReply(this);
        }
    }
}
