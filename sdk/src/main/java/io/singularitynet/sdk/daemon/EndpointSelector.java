package io.singularitynet.sdk.daemon;

// FIXME rename to EndpointSelectionStrategy
/**
 * Next endpoint selection strategy. Daemon connection instance can use it to
 * get new endpoint when connection fails.
 */
public interface EndpointSelector {
    
    /**
     * Method returns next endpoint.
     * @return next endpoint instance.
     */
    Endpoint nextEndpoint();

}
