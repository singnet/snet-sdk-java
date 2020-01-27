package io.singularitynet.sdk.registry;

public interface MetadataProvider {

    OrganizationMetadata getOrganizationMetadata();
    ServiceMetadata getServiceMetadata();

}
