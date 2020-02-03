package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.google.gson.annotations.SerializedName;

@EqualsAndHashCode
@ToString
public class OrganizationMetadata {

    private final String orgName;
    private final String orgId;
    @SerializedName("groups") private final List<PaymentGroup> paymentGroups;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private OrganizationMetadata(Builder builder) {
        this.orgName = builder.orgName;
        this.orgId = builder.orgId;
        this.paymentGroups = builder.paymentGroups;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getOrgId() {
        return orgId;
    }

    public List<PaymentGroup> getPaymentGroups() {
        return paymentGroups;
    }

    public Optional<PaymentGroup> getPaymentGroupById(PaymentGroupId paymentGroupId) {
        return paymentGroups.stream()
            .filter(group -> group.getPaymentGroupId().equals(paymentGroupId))
            .findFirst();
    }

    public static class Builder {

        private String orgName;
        private String orgId;
        private List<PaymentGroup> paymentGroups = new ArrayList<>();

        private Builder() {
        }

        private Builder(OrganizationMetadata object) {
            this.orgName = object.orgName;
            this.orgId = object.orgId;
            this.paymentGroups = object.paymentGroups;
        }

        public Builder setOrgName(String orgName) {
            this.orgName = orgName;
            return this;
        }

        public Builder setOrgId(String orgId) {
            this.orgId = orgId;
            return this;
        }

        public Builder addPaymentGroup(PaymentGroup paymentGroup) {
            this.paymentGroups.add(paymentGroup);
            return this;
        }

        public OrganizationMetadata build() {
            return new OrganizationMetadata(this);
        }
    }
}
