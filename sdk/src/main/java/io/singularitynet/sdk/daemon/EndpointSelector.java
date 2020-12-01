package io.singularitynet.sdk.daemon;

import io.singularitynet.sdk.registry.MetadataProvider;

/**
 * Next endpoint selection strategy. Daemon connection instance can use it to
 * get new endpoint when connection fails.
 */
public interface EndpointSelector {
    
    /**
     * Method returns next endpoint.
     * @param metadataProvider service metadata provider.
     * @return next endpoint instance.
     */
    Endpoint nextEndpoint(MetadataProvider metadataProvider);

}
