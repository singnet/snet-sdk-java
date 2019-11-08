package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigInteger;

import io.singularitynet.sdk.ethereum.Address;

@EqualsAndHashCode
@ToString
public class PaymentDetails {

    private final String paymentAddress;
    private final BigInteger paymentExpirationThreshold;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private PaymentDetails(Builder builder) {
        this.paymentAddress = builder.paymentAddress;
        this.paymentExpirationThreshold = builder.paymentExpirationThreshold;
    }

    public Address getPaymentAddress() {
        return new Address(paymentAddress);
    }

    public BigInteger getPaymentExpirationThreshold() {
        return paymentExpirationThreshold;
    }

    public static class Builder {

        private String paymentAddress;
        private BigInteger paymentExpirationThreshold;

        private Builder() {
        }

        private Builder(PaymentDetails object) {
            this.paymentAddress = object.paymentAddress;
            this.paymentExpirationThreshold = object.paymentExpirationThreshold;
        }

        public Builder setPaymentAddress(Address paymentAddress) {
            this.paymentAddress = paymentAddress.toString();
            return this;
        }

        public Builder setPaymentExpirationThreshold(BigInteger paymentExpirationThreshold) {
            this.paymentExpirationThreshold = paymentExpirationThreshold;
            return this;
        }

        public PaymentDetails build() {
            return new PaymentDetails(this);
        }
    }
}
