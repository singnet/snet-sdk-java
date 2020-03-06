package io.singularitynet.sdk.daemon;

import java.net.URL;

import io.singularitynet.sdk.registry.EndpointGroup;

/**
 * Information about endpoint.
 */
public interface Endpoint {

    /**
     * Return endpoint group instance.
     */
    EndpointGroup getGroup();

    /**
     * Return endpoint URL.
     */
    URL getUrl();

}
