package io.singularitynet.sdk.registry;

/**
 * Caching metadata provider implementation. This class gets metadata from
 * underlying metadata provider once and returns it on each subsequent call.
 */
public class CachingMetadataProvider implements MetadataProvider {

    private final OrganizationMetadata orgMetadata;

    /**
     * @param delegate underlaying metadata provider.
     */
    public CachingMetadataProvider(MetadataProvider delegate) {
        this.orgMetadata = delegate.getOrganizationMetadata();
    }

    @Override
    public OrganizationMetadata getOrganizationMetadata() {
        return orgMetadata;
    }

    @Override
    public ServiceMetadata getServiceMetadata() {
        return null;
    }
}
