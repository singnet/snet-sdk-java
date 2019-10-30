package io.singularitynet.sdk.ethereum;

import org.junit.*;
import static org.junit.Assert.*;

public class AddressTest {

    @Test
    public void addressToByteArrayPrefix() {
        byte[] result = new Address("0xf25186B5081Ff5cE73482AD761DB0eB0d25abfBF").toByteArray();

        assertArrayEquals("Contract address converted",
                new byte[] { (byte) 0xf2, 0x51, (byte) 0x86, (byte) 0xB5, 0x08, 0x1F, (byte) 0xf5, (byte) 0xcE, 0x73, 0x48, 0x2A, (byte) 0xD7, 0x61, (byte) 0xDB, 0x0e, (byte) 0xB0, (byte) 0xd2, (byte) 0x5a, (byte) 0xbf, (byte) 0xBF },
                result);
    }

    @Test
    public void addressToByteArrayNoPrefix() {
        byte[] result = new Address("f25186B5081Ff5cE73482AD761DB0eB0d25abfBF").toByteArray();

        assertArrayEquals("Contract address converted",
                new byte[] { (byte) 0xf2, 0x51, (byte) 0x86, (byte) 0xB5, 0x08, 0x1F, (byte) 0xf5, (byte) 0xcE, 0x73, 0x48, 0x2A, (byte) 0xD7, 0x61, (byte) 0xDB, 0x0e, (byte) 0xB0, (byte) 0xd2, (byte) 0x5a, (byte) 0xbf, (byte) 0xBF },
                result);
    }

}
