package io.singularitynet.sdk.mpe;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigInteger;
import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode
@ToString
public class PaymentChannel {

    private final BigInteger channelId;
    private final String mpeContractAddress;
    private final BigInteger nonce;
    private final String sender;
    private final String signer;
    private final String recipient;
    private final byte[] paymentGroupId;
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

    public String getMpeContractAddress() {
        return mpeContractAddress;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public String getSender() {
        return sender;
    }

    public String getSigner() {
        return signer;
    }

    public String getRecipient() {
        return recipient;
    }

    public byte[] getPaymentGroupId() {
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
        private String mpeContractAddress;
        private BigInteger nonce;
        private String sender;
        private String signer;
        private String recipient;
        private byte[] paymentGroupId;
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

        public Builder setMpeContractAddress(String mpeContractAddress) {
            this.mpeContractAddress = mpeContractAddress;
            return this;
        }

        public Builder setNonce(BigInteger nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder setSender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder setSigner(String signer) {
            this.signer = signer;
            return this;
        }

        public Builder setRecipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder setPaymentGroupId(byte[] paymentGroupId) {
            checkArgument(paymentGroupId.length == 32, "Payment group id should be 32 bytes length");
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
