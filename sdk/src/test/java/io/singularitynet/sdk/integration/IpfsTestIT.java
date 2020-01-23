package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import io.ipfs.api.*;
import io.ipfs.multihash.*;

public class IpfsTestIT {

    private static final String TEST_FILE_CONTENTS =  "This is IPFS test file";
    private static final String TEST_FILE_IPFS_HASH = "QmPJfvvnALcyCL4BiXWh6Nou5472kzC2E3YKDH3XDaRPe6";

    @Test
    public void saveLoadFileFromSingularityNetIpfs() throws IOException {
        IPFS ipfs = new IPFS("localhost", 5002);

        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("test.txt", TEST_FILE_CONTENTS.getBytes());
        MerkleNode added = ipfs.add(file).get(0);
        assertEquals("IPFS test file hash", Multihash.fromBase58(TEST_FILE_IPFS_HASH), added.hash);

        Multihash filePointer = Multihash.fromBase58(TEST_FILE_IPFS_HASH);
        byte[] fileContents = ipfs.cat(filePointer);
        assertEquals("IPFS test file", TEST_FILE_CONTENTS, new String(fileContents));
    }

    @Test
    public void newIpfsUri() throws Exception {
        URI uri = new URI("ipfs://Qma2KoWcf7f3c1m9nbr27LoPPHGonBBaTZeuxJ9L48CLS1");

        assertEquals("URI authority", "Qma2KoWcf7f3c1m9nbr27LoPPHGonBBaTZeuxJ9L48CLS1", uri.getAuthority());
    }
}
