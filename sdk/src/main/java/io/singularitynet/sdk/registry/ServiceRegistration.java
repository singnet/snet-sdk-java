package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;

@EqualsAndHashCode
@ToString
public class ServiceRegistration {

    private final String serviceId;
    private final URI metadataUri;
    private final List<String> tags;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private ServiceRegistration(Builder builder) {
        this.serviceId = builder.serviceId;
        this.metadataUri = builder.metadataUri;
        this.tags = builder.tags;
    }

    public String getServiceId() {
        return serviceId;
    }

    public URI getMetadataUri() {
        return metadataUri;
    }

    public List<String> getTags() {
        return tags;
    }

    public static class Builder {

        private String serviceId;
        private URI metadataUri;
        private List<String> tags = new ArrayList<>();

        private Builder() {
        }

        private Builder(ServiceRegistration object) {
            this.serviceId = object.serviceId;
            this.metadataUri = object.metadataUri;
            this.tags = object.tags;
        }

        public Builder setServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder setMetadataUri(URI metadataUri) {
            this.metadataUri = metadataUri;
            return this;
        }

        public Builder addTag(String tag) {
            this.tags.add(tag);
            return this;
        }

        public ServiceRegistration build() {
            return new ServiceRegistration(this);
        }
    }
}
