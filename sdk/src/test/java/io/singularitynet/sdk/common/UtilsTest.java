package io.singularitynet.sdk.common;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Arrays;

public class UtilsTest {

    private static final byte[] ALL_BYTES_ARRAY;

    static {
        ALL_BYTES_ARRAY = new byte[256];
        for (int i = 0; i < 256; i++) {
            ALL_BYTES_ARRAY[i] = (byte) i;
        }
    }

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
    public void bytes32ToStr32CharLength() {
        byte[] bytes = new byte[32];
        Arrays.fill(bytes, (byte)'x');

        String str = Utils.bytes32ToStr(bytes);

        assertEquals("Max length string", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", str);
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

    @Test
    public void bytesToBase64() {
        String str = Utils.bytesToBase64(ALL_BYTES_ARRAY);

        String expected = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4" +
            "OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3Bx" +
            "cnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmq" +
            "q6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj" +
            "5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w==";
        assertEquals("Base64 encoded", expected, str);
    }

    @Test
    public void bytesToBase64OneByte() {
        String str = Utils.bytesToBase64(new byte[] { 0x7F });

        assertEquals("Base64 encoded", "fw==", str);
    }

    @Test
    public void bytesToBase64TwoBytes() {
        String str = Utils.bytesToBase64(new byte[] { 0x7F, 0x7F });

        assertEquals("Base64 encoded", "f38=", str);
    }

    @Test
    public void bytesToBase64ThreeBytes() {
        String str = Utils.bytesToBase64(new byte[] { 0x7F, 0x7F, 0x7F });

        assertEquals("Base64 encoded", "f39/", str);
    }

    @Test
    public void bytesToBase64ZeroBytes() {
        String str = Utils.bytesToBase64(new byte[0]);

        assertEquals("Base64 encoded", "", str);
    }

    @Test
    public void base64ToBytes() {
        String str = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4" +
            "OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3Bx" +
            "cnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmq" +
            "q6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj" +
            "5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w==";
        
        byte[] bytes = Utils.base64ToBytes(str);

        assertArrayEquals("Base64 decoded", ALL_BYTES_ARRAY, bytes);
    }

    @Test
    public void base64ToBytesOneByte() {
        byte[] bytes = Utils.base64ToBytes("fw==");

        assertArrayEquals("Base64 decoded", new byte[] { 0x7F }, bytes);
    }

    @Test
    public void base64ToBytesTwoBytes() {
        byte[] bytes = Utils.base64ToBytes("f38=");

        assertArrayEquals("Base64 decoded", new byte[] { 0x7F, 0x7F }, bytes);
    }

    @Test
    public void base64ToBytesThreeBytes() {
        byte[] bytes = Utils.base64ToBytes("f39/");

        assertArrayEquals("Base64 decoded", new byte[] { 0x7F, 0x7F, 0x7F }, bytes);
    }

    @Test
    public void base64ToBytesZeroBytes() {
        byte[] bytes = Utils.base64ToBytes("");

        assertArrayEquals("Base64 decoded", new byte[0], bytes);
    }

}
