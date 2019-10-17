package io.singularitynet.sdk.registry;

import java.net.URI;
import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;

import static io.singularitynet.sdk.registry.Utils.*;

public class IpfsMetadataStorage implements MetadataStorage {

    private final IPFS ipfs;

    public IpfsMetadataStorage(IPFS ipfs) {
        this.ipfs = ipfs;
    }

    @Override
    public byte[] get(URI uri) {
        return rethrowChecked(() -> {
            Multihash filePointer = Multihash.fromBase58(uri.getAuthority());
            return ipfs.cat(filePointer);
        });
    }
}
