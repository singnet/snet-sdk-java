package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;

@EqualsAndHashCode
@ToString
public class OrganizationRegistration {

    private final String orgId;
    private final URI metadataUri;
    private final List<String> serviceIds;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private OrganizationRegistration(Builder builder) {
        this.orgId = builder.orgId;
        this.metadataUri = builder.metadataUri;
        this.serviceIds = builder.serviceIds;
    }

    public String getOrgId() {
        return orgId;
    }

    public URI getMetadataUri() {
        return metadataUri;
    }

    public List<String> getServiceIds() {
        return serviceIds;
    }

    public static class Builder {

        private String orgId;
        private URI metadataUri;
        private List<String> serviceIds = new ArrayList<>();

        private Builder() {
        }

        private Builder(OrganizationRegistration object) {
            this.orgId = object.orgId;
            this.metadataUri = object.metadataUri;
            this.serviceIds = object.serviceIds;
        }

        public Builder setOrgId(String orgId) {
            this.orgId = orgId;
            return this;
        }

        public Builder setMetadataUri(URI metadataUri) {
            this.metadataUri = metadataUri;
            return this;
        }

        public Builder addServiceId(String serviceId) {
            this.serviceIds.add(serviceId);
            return this;
        }

        public OrganizationRegistration build() {
            return new OrganizationRegistration(this);
        }
    }
}
