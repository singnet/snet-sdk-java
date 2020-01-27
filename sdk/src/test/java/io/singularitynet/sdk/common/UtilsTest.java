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

    private static final String ALL_BYTES_HEX =
        "000102030405060708090A0B0C0D0E0F" +
        "101112131415161718191A1B1C1D1E1F" +
        "202122232425262728292A2B2C2D2E2F" +
        "303132333435363738393A3B3C3D3E3F" +
        "404142434445464748494A4B4C4D4E4F" +
        "505152535455565758595A5B5C5D5E5F" +
        "606162636465666768696A6B6C6D6E6F" +
        "707172737475767778797A7B7C7D7E7F" +
        "808182838485868788898A8B8C8D8E8F" +
        "909192939495969798999A9B9C9D9E9F" +
        "A0A1A2A3A4A5A6A7A8A9AAABACADAEAF" +
        "B0B1B2B3B4B5B6B7B8B9BABBBCBDBEBF" +
        "C0C1C2C3C4C5C6C7C8C9CACBCCCDCECF" +
        "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF" +
        "E0E1E2E3E4E5E6E7E8E9EAEBECEDEEEF" +
        "F0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF";

    private static final String ALL_BYTES_BASE64 =
        "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4" +
        "OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3Bx" +
        "cnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmq" +
        "q6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj" +
        "5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w==";

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

        assertEquals("Base64 encoded", ALL_BYTES_BASE64, str);
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
        byte[] bytes = Utils.base64ToBytes(ALL_BYTES_BASE64);

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

    @Test
    public void bytesToHex() {
        String hex = Utils.bytesToHex(ALL_BYTES_ARRAY);

        assertEquals("Hex encoded", ALL_BYTES_HEX, hex);
    }

    @Test
    public void hexToBytes() {
        byte[] bytes = Utils.hexToBytes(ALL_BYTES_HEX);

        assertArrayEquals("Hex decoded", ALL_BYTES_ARRAY, bytes);
    }

    @Test
    public void bytesToHexWithSpaces() {
        String hex = Utils.bytesToHexWithSpaces(ALL_BYTES_ARRAY);

        assertEquals("Hex encoded",
                "00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F  " +
                "10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F  " +
                "20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F  " +
                "30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F  " +
                "40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F  " +
                "50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F  " +
                "60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F  " +
                "70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F  " +
                "80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F  " +
                "90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F  " +
                "A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF  " +
                "B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF  " +
                "C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF  " +
                "D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF  " +
                "E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF  " +
                "F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF  ",
                hex);
    }

}
