package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.net.URI;

@EqualsAndHashCode
@ToString
public class ServiceRegistration {

    private final String serviceId;
    private final URI metadataUri;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private ServiceRegistration(Builder builder) {
        this.serviceId = builder.serviceId;
        this.metadataUri = builder.metadataUri;
    }

    public String getServiceId() {
        return serviceId;
    }

    public URI getMetadataUri() {
        return metadataUri;
    }

    public static class Builder {

        private String serviceId;
        private URI metadataUri;

        private Builder() {
        }

        private Builder(ServiceRegistration object) {
            this.serviceId = object.serviceId;
            this.metadataUri = object.metadataUri;
        }

        public Builder setServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder setMetadataUri(URI metadataUri) {
            this.metadataUri = metadataUri;
            return this;
        }

        public ServiceRegistration build() {
            return new ServiceRegistration(this);
        }
    }
}
