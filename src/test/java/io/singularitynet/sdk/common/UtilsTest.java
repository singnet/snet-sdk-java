package io.singularitynet.sdk.common;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;

public class UtilsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void wrapExceptionRethrowsCheckedException() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("test exception");

        Utils.wrapExceptions(() -> {
            throw new Exception("test exception");
        });
    }

    @Test
    public void strToBytes32ChecksArgumentLength() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Passed string length exceeds 32 bytes");

        Utils.strToBytes32("012345678901234567890123456789012");
    }

    @Test
    public void strToBytes32AddsZeroPadding() {
        byte[] bytes32 = Utils.strToBytes32("");

        assertArrayEquals("Bytes32 from empty string", new byte[32], bytes32);
    }

    @Test
    public void bytes32ToStrCheckArgumentNotTooLong() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Passed array length is not equal to 32 bytes");

        Utils.bytes32ToStr(new byte[33]);
    }

    @Test
    public void bytes32ToStrCheckArgumentNotTooShort() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Passed array length is not equal to 32 bytes");

        Utils.bytes32ToStr(new byte[31]);
    }
}
