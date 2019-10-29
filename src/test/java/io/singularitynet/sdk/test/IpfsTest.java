package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import io.ipfs.api.*;
import io.ipfs.multihash.*;

public class IpfsTest {

    @Test
    public void loadFileFromSingularityNetIpfs() throws IOException {
        IPFS ipfs = new IPFS("ipfs.singularitynet.io", 80);
        Multihash filePointer = Multihash.fromBase58("QmNt1HZ5HWDTGusMuWQnRQCEeug7z55tSesCeKvjhATfTG");
        byte[] fileContents = ipfs.cat(filePointer);
        assertTrue("Non empty file from IPFS or exception is expected", fileContents.length > 0);
    }

    @Test
    public void newIpfsUri() throws Exception {
        URI uri = new URI("ipfs://Qma2KoWcf7f3c1m9nbr27LoPPHGonBBaTZeuxJ9L48CLS1");

        assertEquals("URI authority", "Qma2KoWcf7f3c1m9nbr27LoPPHGonBBaTZeuxJ9L48CLS1", uri.getAuthority());
    }
}
