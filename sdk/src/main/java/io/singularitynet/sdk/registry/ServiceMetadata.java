package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;

import io.singularitynet.sdk.ethereum.Address;

@EqualsAndHashCode
@ToString
public class ServiceMetadata {

    private final String displayName;
    private final String modelIpfsHash;
    private final String mpeAddress;
    @SerializedName("groups") private final List<EndpointGroup> endpointGroups;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private ServiceMetadata(Builder builder) {
        this.displayName = builder.displayName;
        this.modelIpfsHash = builder.modelIpfsHash;
        this.mpeAddress = builder.mpeAddress;
        this.endpointGroups = builder.endpointGroups;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getModelIpfsHash() {
        return modelIpfsHash;
    }

    public Address getMpeAddress() {
        return new Address(mpeAddress);
    }

    public List<EndpointGroup> getEndpointGroups() {
        return endpointGroups;
    }

    public static class Builder {

        private String displayName;
        private String modelIpfsHash;
        private String mpeAddress;
        private List<EndpointGroup> endpointGroups = new ArrayList<>();

        private Builder() {
        }

        private Builder(ServiceMetadata object) {
            this.displayName = object.displayName;
            this.modelIpfsHash = object.modelIpfsHash;
            this.mpeAddress = object.mpeAddress;
            this.endpointGroups = object.endpointGroups;
        }

        public Builder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder setModelIpfsHash(String modelIpfsHash) {
            this.modelIpfsHash = modelIpfsHash;
            return this;
        }

        public Builder setMpeAddress(Address mpeAddress) {
            this.mpeAddress = mpeAddress.toString();
            return this;
        }

        public Builder addEndpointGroup(EndpointGroup endpointGroup) {
            this.endpointGroups.add(endpointGroup);
            return this;
        }

        public Builder clearEndpointGroups() {
            this.endpointGroups.clear();
            return this;
        }

        public ServiceMetadata build() {
            return new ServiceMetadata(this);
        }
    }
}
