package io.singularitynet.sdk.common;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.concurrent.Callable;
import java.util.Base64;
import org.web3j.abi.datatypes.Address;

public class Utils {

    public static byte[] strToBytes(String str) {
        return str.getBytes(UTF_8);
    }

    public static byte[] strToBytes32(String str) {
        byte[] bytes32 = new byte[32];
        int i = 0;
        for (byte b : str.getBytes(UTF_8)) {
            bytes32[i++] = b;
        }
        return bytes32;
    }

    public static byte[] base64ToBytes(String str) {
        return Base64.getDecoder().decode(str);
    }

    public static String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String bytes32ToStr(byte[] bytes) {
        String full = new String(bytes, UTF_8);
        return full.substring(0, full.indexOf(0));
    }

    public static String bytesToStr(byte[] bytes) {
        return new String(bytes, UTF_8);
    }

    // TODO: replace by Address.toByteArray()
    public static byte[] addressToBytes(String address) {
        return new Address(address).toUint().getValue().toByteArray();
    }

    public static <T> T wrapExceptions(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
