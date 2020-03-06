package io.singularitynet.sdk.freecall;

import org.junit.*;
import static org.junit.Assert.*;

import java.math.BigInteger;

public class FreeCallAuthTokenTest {

    @Test
    public void testToString() {
        FreeCallAuthToken token = FreeCallAuthToken.newBuilder()
            .setDappUserId("user@mail.com")
            .setExpirationBlock(BigInteger.valueOf(1234))
            .setToken("11121314FF")
            .build();

        String toString = token.toString();

        assertEquals("toString() results", "FreeCallAuthToken(dappUserId=user@mail.com, expirationBlock=1234, token=11121314FF)",
                toString);
    }

}
