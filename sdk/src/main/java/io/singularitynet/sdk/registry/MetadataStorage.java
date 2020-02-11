package io.singularitynet.sdk.registry;

import java.net.URI;

/**
 * Abstract metadata storage interface.
 */
public interface MetadataStorage {

    /**
     * Return metadata bytes for given URI.
     * @param uri URI which points to metadata instance.
     * @return metadata bytes.
     */
    byte[] get(URI uri);

}
