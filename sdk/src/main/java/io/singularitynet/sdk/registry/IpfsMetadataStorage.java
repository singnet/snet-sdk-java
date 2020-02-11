package io.singularitynet.sdk.registry;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.common.Utils;

/**
 * Metadata storage based on IPFS implementation.
 */
public class IpfsMetadataStorage implements MetadataStorage {

    private final static Logger log = LoggerFactory.getLogger(IpfsMetadataStorage.class);

    private final IPFS ipfs;

    /**
     * New metadata storage for IPFS client instance.
     * @param ipfs IPFS client instance.
     */
    public IpfsMetadataStorage(IPFS ipfs) {
        this.ipfs = ipfs;
    }

    @Override
    public byte[] get(URI uri) {
        return Utils.wrapExceptions(() -> {
            log.info("Get data from IPFS, uri: {}", uri);
            Multihash filePointer = Multihash.fromBase58(uri.getAuthority());
            byte[] metadata = ipfs.cat(filePointer);
            log.info("{} bytes received", metadata.length);
            return metadata;
        });
    }
}
