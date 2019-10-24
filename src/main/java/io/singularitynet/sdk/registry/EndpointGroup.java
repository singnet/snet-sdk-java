package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;
import static com.google.common.base.Preconditions.checkArgument;

import io.singularitynet.sdk.common.Utils;

@EqualsAndHashCode
@ToString
public class EndpointGroup {

    private final String groupName;
    private final List<Pricing> pricing;
    private final List<URL> endpoints;
    // TODO: replace by GroupId class to implement toString() and seemless JSON
    // conversion
    @SerializedName("group_id") private final String paymentGroupId;

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

    public byte[] getPaymentGroupId() {
        return Utils.base64ToBytes(paymentGroupId);
    }

    public static class Builder {

        private String groupName;
        private List<Pricing> pricing = new ArrayList<>();
        private List<URL> endpoints = new ArrayList<>();
        private String paymentGroupId;

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

        public Builder addPricing(Pricing pricing) {
            this.pricing.add(pricing);
            return this;
        }

        public Builder addEndpoint(URL endpoint) {
            this.endpoints.add(endpoint);
            return this;
        }

        public Builder setPaymentGroupId(byte[] paymentGroupId) {
            checkArgument(paymentGroupId.length == 32, "Payment group id should be 32 bytes length");
            this.paymentGroupId = Utils.bytesToBase64(paymentGroupId);
            return this;
        }

        public EndpointGroup build() {
            return new EndpointGroup(this);
        }
    }
}
