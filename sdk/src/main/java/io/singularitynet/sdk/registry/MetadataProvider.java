package io.singularitynet.sdk.registry;

/**
 * Interface provides access to the metdata of the specific SingularityNet
 * service.
 */
public interface MetadataProvider {

    /**
     * Get publisher organization related metadata.
     * @return organization metadata.
     */
    OrganizationMetadata getOrganizationMetadata();

    /**
     * Return service metadata.
     * @return service metadata.
     */
    ServiceMetadata getServiceMetadata();

}
