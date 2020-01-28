package io.singularitynet.sdk.mpe;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigInteger;

import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.registry.PaymentGroupId;

@EqualsAndHashCode
@ToString
public class PaymentChannel {

    private final BigInteger channelId;
    private final Address mpeContractAddress;
    private final BigInteger nonce;
    private final Address sender;
    private final Address signer;
    private final Address recipient;
    private final PaymentGroupId paymentGroupId;
    private final BigInteger value;
    private final BigInteger expiration;
    private final BigInteger spentAmount;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private PaymentChannel(Builder builder) {
        this.channelId = builder.channelId;
        this.mpeContractAddress = builder.mpeContractAddress;
        this.nonce = builder.nonce;
        this.sender = builder.sender;
        this.signer = builder.signer;
        this.recipient = builder.recipient;
        this.paymentGroupId = builder.paymentGroupId;
        this.value = builder.value;
        this.expiration = builder.expiration;
        this.spentAmount = builder.spentAmount;
    }

    public BigInteger getChannelId() {
        return channelId;
    }

    public Address getMpeContractAddress() {
        return mpeContractAddress;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public Address getSender() {
        return sender;
    }

    public Address getSigner() {
        return signer;
    }

    public Address getRecipient() {
        return recipient;
    }

    public PaymentGroupId getPaymentGroupId() {
        return paymentGroupId;
    }

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getExpiration() {
        return expiration;
    }

    public BigInteger getSpentAmount() {
        return spentAmount;
    }

    public static class Builder {

        private BigInteger channelId;
        private Address mpeContractAddress;
        private BigInteger nonce;
        private Address sender;
        private Address signer;
        private Address recipient;
        private PaymentGroupId paymentGroupId;
        private BigInteger value;
        private BigInteger expiration;
        private BigInteger spentAmount;

        private Builder() {
        }

        private Builder(PaymentChannel object) {
            this.channelId = object.channelId;
            this.mpeContractAddress = object.mpeContractAddress;
            this.nonce = object.nonce;
            this.sender = object.sender;
            this.signer = object.signer;
            this.recipient = object.recipient;
            this.paymentGroupId = object.paymentGroupId;
            this.value = object.value;
            this.expiration = object.expiration;
            this.spentAmount = object.spentAmount;
        }

        public Builder setChannelId(BigInteger channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder setMpeContractAddress(Address mpeContractAddress) {
            this.mpeContractAddress = mpeContractAddress;
            return this;
        }

        public Builder setNonce(BigInteger nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder setSender(Address sender) {
            this.sender = sender;
            return this;
        }

        public Builder setSigner(Address signer) {
            this.signer = signer;
            return this;
        }

        public Builder setRecipient(Address recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder setPaymentGroupId(PaymentGroupId paymentGroupId) {
            this.paymentGroupId = paymentGroupId;
            return this;
        }

        public Builder setValue(BigInteger value) {
            this.value = value;
            return this;
        }

        public Builder setExpiration(BigInteger expiration) {
            this.expiration = expiration;
            return this;
        }

        public Builder setSpentAmount(BigInteger spentAmount) {
            this.spentAmount = spentAmount;
            return this;
        }

        public PaymentChannel build() {
            return new PaymentChannel(this);
        }
    }
}
