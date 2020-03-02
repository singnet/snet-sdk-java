package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;

import io.singularitynet.sdk.ethereum.Address;

@EqualsAndHashCode
@ToString
public class EndpointGroup {

    private final String groupName;
    private final List<Pricing> pricing;
    private final List<URL> endpoints;
    @SerializedName("group_id") private final PaymentGroupId paymentGroupId;
    private final long freeCalls;
    private final Address freeCallSignerAddress;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private EndpointGroup(Builder builder) {
        this.groupName = builder.groupName;
        this.pricing = builder.pricing;
        this.endpoints = builder.endpoints;
        this.paymentGroupId = builder.paymentGroupId;
        this.freeCalls = builder.freeCalls;
        this.freeCallSignerAddress = builder.freeCallSignerAddress;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<Pricing> getPricing() {
        return pricing;
    }

    public List<URL> getEndpoints() {
        return endpoints;
    }

    public PaymentGroupId getPaymentGroupId() {
        return paymentGroupId;
    }

    public long getFreeCalls() {
        return freeCalls;
    }

    public Address getFreeCallSignerAddress() {
        return freeCallSignerAddress;
    }

    public static class Builder {

        private String groupName;
        private List<Pricing> pricing = new ArrayList<>();
        private List<URL> endpoints = new ArrayList<>();
        private PaymentGroupId paymentGroupId;
        private long freeCalls;
        private Address freeCallSignerAddress;

        private Builder() {
        }

        private Builder(EndpointGroup object) {
            this.groupName = object.groupName;
            this.pricing = object.pricing;
            this.endpoints = object.endpoints;
            this.paymentGroupId = object.paymentGroupId;
            this.freeCalls = object.freeCalls;
            this.freeCallSignerAddress = object.freeCallSignerAddress;
        }

        public Builder setGroupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public String getGroupName() {
            return groupName;
        }

        public Builder setPricing(List<Pricing> pricing) {
            this.pricing = pricing;
            return this;
        }

        public Builder addPricing(Pricing pricing) {
            this.pricing.add(pricing);
            return this;
        }

        public Builder clearPricing() {
            this.pricing.clear();
            return this;
        }

        public List<Pricing> getPricing() {
            return pricing;
        }

        public Builder setEndpoints(List<URL> endpoints) {
            this.endpoints = endpoints;
            return this;
        }

        public Builder addEndpoint(URL endpoint) {
            this.endpoints.add(endpoint);
            return this;
        }

        public Builder clearEndpoints() {
            this.endpoints.clear();
            return this;
        }

        public List<URL> getEndpoints() {
            return endpoints;
        }

        public Builder setPaymentGroupId(PaymentGroupId paymentGroupId) {
            this.paymentGroupId = paymentGroupId;
            return this;
        }

        public PaymentGroupId getPaymentGroupId() {
            return paymentGroupId;
        }

        public Builder setFreeCalls(long freeCalls) {
            this.freeCalls = freeCalls;
            return this;
        }

        public long getFreeCalls() {
            return freeCalls;
        }

        public Builder setFreeCallSignerAddress(Address freeCallSignerAddress) {
            this.freeCallSignerAddress = freeCallSignerAddress;
            return this;
        }

        public Address getFreeCallSignerAddress() {
            return freeCallSignerAddress;
        }

        public EndpointGroup build() {
            return new EndpointGroup(this);
        }
    }
}
