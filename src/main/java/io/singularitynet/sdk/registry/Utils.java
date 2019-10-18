package io.singularitynet.sdk.registry;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.concurrent.Callable;

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

    public static String bytes32ToStr(byte[] bytes) {
        String full = new String(bytes, UTF_8);
        return full.substring(0, full.indexOf(0));
    }

    public static String bytesToStr(byte[] bytes) {
        return new String(bytes, UTF_8);
    }

    public static <T> T wrapExceptions(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
