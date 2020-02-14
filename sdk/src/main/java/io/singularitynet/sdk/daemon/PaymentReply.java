package io.singularitynet.sdk.daemon;

import java.math.BigInteger;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import io.singularitynet.sdk.ethereum.Signature;

@EqualsAndHashCode
@ToString
public class PaymentReply {

    private final BigInteger channelId;
    private final BigInteger channelNonce;
    private final BigInteger signedAmount;
    private final Signature signature;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private PaymentReply(Builder builder) {
        this.channelId = builder.channelId;
        this.channelNonce = builder.channelNonce;
        this.signedAmount = builder.signedAmount;
        this.signature = builder.signature;
    }

    public BigInteger getChannelId() {
        return channelId;
    }

    public BigInteger getChannelNonce() {
        return channelNonce;
    }

    public BigInteger getSignedAmount() {
        return signedAmount;
    }

    public Signature getSignature() {
        return signature;
    }

    public static class Builder {

        private BigInteger channelId;
        private BigInteger channelNonce;
        private BigInteger signedAmount;
        private Signature signature;

        private Builder() {
        }

        private Builder(PaymentReply object) {
            this.channelId = object.channelId;
            this.channelNonce = object.channelNonce;
            this.signedAmount = object.signedAmount;
            this.signature = object.signature;
        }

        public Builder setChannelId(BigInteger channelId) {
            this.channelId = channelId;
            return this;
        }

        public BigInteger getChannelId() {
            return channelId;
        }

        public Builder setChannelNonce(BigInteger channelNonce) {
            this.channelNonce = channelNonce;
            return this;
        }

        public BigInteger getChannelNonce() {
            return channelNonce;
        }

        public Builder setSignedAmount(BigInteger signedAmount) {
            this.signedAmount = signedAmount;
            return this;
        }

        public BigInteger getSignedAmount() {
            return signedAmount;
        }

        public Builder setSignature(Signature signature) {
            this.signature = signature;
            return this;
        }

        public Signature getSignature() {
            return signature;
        }

        public PaymentReply build() {
            return new PaymentReply(this);
        }
    }
}
