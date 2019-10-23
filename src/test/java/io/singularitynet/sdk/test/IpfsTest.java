package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;

import io.ipfs.api.*;
import io.ipfs.multihash.*;
import java.io.IOException;

public class IpfsTest {

    @Test
    public void loadFileFromSingularityNetIpfs() throws IOException {
        IPFS ipfs = new IPFS("ipfs.singularitynet.io", 80);
        Multihash filePointer = Multihash.fromBase58("QmNt1HZ5HWDTGusMuWQnRQCEeug7z55tSesCeKvjhATfTG");
        byte[] fileContents = ipfs.cat(filePointer);
        assertTrue("Non empty file from IPFS or exception is expected", fileContents.length > 0);
    }
}
