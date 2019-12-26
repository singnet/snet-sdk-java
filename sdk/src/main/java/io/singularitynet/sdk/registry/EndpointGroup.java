package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;

@EqualsAndHashCode
@ToString
public class EndpointGroup {

    private final String groupName;
    private final List<Pricing> pricing;
    private final List<URL> endpoints;
    @SerializedName("group_id") private final PaymentGroupId paymentGroupId;

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

    public static class Builder {

        private String groupName;
        private List<Pricing> pricing = new ArrayList<>();
        private List<URL> endpoints = new ArrayList<>();
        private PaymentGroupId paymentGroupId;

        private Builder() {
        }

        private Builder(EndpointGroup object) {
            this.groupName = object.groupName;
            this.pricing = object.pricing;
            this.endpoints = object.endpoints;
            this.paymentGroupId = object.paymentGroupId;
        }

        public Builder setGroupName(String groupName) {
            this.groupName = groupName;
            return this;
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

        public Builder setEndpoints(List<URL> endpoints) {
            this.endpoints = endpoints;
            return this;
        }

        public Builder addEndpoint(URL endpoint) {
            this.endpoints.add(endpoint);
            return this;
        }

        public Builder setPaymentGroupId(PaymentGroupId paymentGroupId) {
            this.paymentGroupId = paymentGroupId;
            return this;
        }

        public EndpointGroup build() {
            return new EndpointGroup(this);
        }
    }
}
