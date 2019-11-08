package io.singularitynet.sdk.registry;

import java.net.URI;

public interface MetadataStorage {

    byte[] get(URI uri);

}
