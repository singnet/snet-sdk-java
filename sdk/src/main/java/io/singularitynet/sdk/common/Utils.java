package io.singularitynet.sdk.common;

import static java.nio.charset.StandardCharsets.UTF_8;
import io.singularitynet.sdk.common.Preconditions;
import java.util.concurrent.Callable;
import java.math.BigInteger;
import java.util.Arrays;

public class Utils {

    private Utils() {
    }

    public static byte[] strToBytes(String str) {
        return str.getBytes(UTF_8);
    }

    public static String bytesToStr(byte[] bytes) {
        String str = new String(bytes, UTF_8);
        int zeroPos = str.indexOf(0);
        if (zeroPos == -1) {
            return str;
        }
        return str.substring(0, zeroPos);
    }

    public static byte[] strToBytes32(String str) {
        Preconditions.checkArgument(str.length() <= 32, "Passed string length exceeds 32 bytes");
        byte[] bytes32 = new byte[32];
        int i = 0;
        for (byte b : str.getBytes(UTF_8)) {
            bytes32[i++] = b;
        }
        return bytes32;
    }

    public static String bytes32ToStr(byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 32, "Passed array length is not equal to 32 bytes");
        String full = new String(bytes, UTF_8);
        int lastZero = full.indexOf(0);
        if (lastZero == -1) {
            return full;
        }
        return full.substring(0, lastZero);
    }

    public static byte[] base64ToBytes(String str) {
        return Base64.decode(str);
    }

    public static String bytesToBase64(byte[] bytes) {
        return Base64.encode(bytes);
    }

    public static byte[] bigIntToBytes32(BigInteger value) {
        byte[] bytes32 = new byte[32];
        byte[] bytes = value.toByteArray();
        // TODO: check bytes length is not greater than 32
        System.arraycopy(bytes, 0, bytes32, 32 - bytes.length, bytes.length);
        return bytes32;
    }

    public static BigInteger bytes32ToBigInt(byte[] bytes) {
        // TODO: check bytes length is equal to 32
        return new BigInteger(bytes);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        StringBuffer hex = new StringBuffer();
        int eight = 0;
        for (byte b : bytes) {
            hex.append(HEX_ARRAY[(b >> 4) & 0xF]).append(HEX_ARRAY[b & 0xF]).append(" ");
            eight++;
            if (eight == 8) {
                hex.append(" ");
                eight = 0;
            }
        }
        return hex.toString();
    }

    public static byte[] hexToBytes(String str) {
        Preconditions.checkArgument(str.length() % 2 == 0, "String should contain even number of hex digits");
        byte[] bytes = new byte[str.length() / 2]; 
        for (int i = 0; i < str.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) +
                Character.digit(str.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static <T> T wrapExceptions(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
