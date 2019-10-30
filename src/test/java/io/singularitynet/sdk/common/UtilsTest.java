package io.singularitynet.sdk.common;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

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

    @Test
    public void addressToBytesPrefixed() {
        byte[] result = Utils.addressToBytes("0x01020E0F");

        assertArrayEquals("Address with prefix converted",
                new byte[] { 0x01, 0x02, 0x0E, 0x0F }, result);
    }

    @Test
    public void addressToBytesNoPrefix() {
        byte[] result = Utils.addressToBytes("F1020E0F");

        assertArrayEquals("Address without prefix converted",
                new byte[] { (byte) 0xF1, 0x02, 0x0E, 0x0F }, result);
    }

    @Test
    public void addressToBytesContractAddress() {
        byte[] result = Utils.addressToBytes("f25186B5081Ff5cE73482AD761DB0eB0d25abfBF");

        assertArrayEquals("Contract address converted",
                new byte[] { (byte) 0xf2, 0x51, (byte) 0x86, (byte) 0xB5, 0x08, 0x1F, (byte) 0xf5, (byte) 0xcE, 0x73, 0x48, 0x2A, (byte) 0xD7, 0x61, (byte) 0xDB, 0x0e, (byte) 0xB0, (byte) 0xd2, (byte) 0x5a, (byte) 0xbf, (byte) 0xBF },
                result);
    }

    @Test
    public void bigIntToBytes32() {
        byte[] bytes32 = Utils.bigIntToBytes32(BigInteger.valueOf(0x1234));

        assertArrayEquals("BigInteger converted to 32 bytes",
                new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x12, 0x34 },
                bytes32);
    }

    @Test
    public void bytes32ToBigInt() {
        BigInteger bigint = Utils.bytes32ToBigInt(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x12, 0x34 });

        assertEquals("BigInteger converted from 32 bytes",
                BigInteger.valueOf(0x1234),
                bigint);
    }

    @Test
    public void bytesToStrTrimTrailingZeros() {
        byte[] bytes = { 't', 'e', 's', 't', 0, 0, 0 };

        String str = Utils.bytesToStr(bytes);

        assertEquals("String from bytes with trailing zeros", "test", str);
    }

    @Test
    public void bytesToStr() {
        byte[] bytes = "test".getBytes();

        String str = Utils.bytesToStr(bytes);

        assertEquals("String from bytes", "test", str);
    }

    @Test
    public void strToBytes() {
        String str = "test";

        byte[] bytes = Utils.strToBytes(str);

        assertArrayEquals("Bytes from string", new byte[] { 't', 'e', 's', 't' },
                bytes);
    }

}
