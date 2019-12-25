package io.singularitynet.sdk.common;

import java.util.HashMap;

public class Base64 {

    private static final String BASE64_ENCODING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private static final HashMap<Character, Integer> BASE64_DECODING;

    static {
        BASE64_DECODING = new HashMap<>();
        for (int i = 0; i < BASE64_ENCODING.length(); ++i) {
            BASE64_DECODING.put(BASE64_ENCODING.charAt(i), i);
        }
    }

    private Base64() {
    }

    public static String encode(byte[] bytes) {
        StringBuffer str = new StringBuffer();

        int i = 0;
        while (i < bytes.length) {

            int triple = 0;
            int inputBytes;
            for (inputBytes = 0; inputBytes < 3 && i < bytes.length; i++, inputBytes++) {
                triple |= (bytes[i] & 0xFF) << (16 - 8 * inputBytes);
            }

            inputBytes++;
            int outputBytes;
            for (outputBytes = 0; outputBytes < 4 && inputBytes > 0; outputBytes++, inputBytes--) {
                int bytePosition = (18 - 6 * outputBytes);
                int alphaIndex = (triple & (0x3F << bytePosition)) >>> bytePosition;
                str.append(BASE64_ENCODING.charAt(alphaIndex));
            }

            while (outputBytes < 4) {
                str.append('=');
                outputBytes++;
            }

        }

        return str.toString();
    }

    public static byte[] decode(String str) {
        int bytesNumber = (str.length() * 6) / 8;
        if (str.length() > 0) {
            if (str.charAt(str.length() - 1) == '=') {
                --bytesNumber;
            }
            if (str.charAt(str.length() - 2) == '=') {
                --bytesNumber;
            }
        }
        byte[] bytes = new byte[bytesNumber];

        int i = 0;
        int o = 0;
        while (o < bytes.length) {

            int triple = 0;
            int inputBytes;
            for (inputBytes = 0; inputBytes < 4; i++, inputBytes++) {
                char c = str.charAt(i);
                if (c == '=') {
                    break;
                }
                int value = BASE64_DECODING.get(c);
                triple |= (value & 0x3F) << (18 - 6 * inputBytes);
            }

            for (int outputBytes = 0; outputBytes < 3 && o < bytes.length; outputBytes++, o++) {
                int bytePosition = (16 - 8 * outputBytes);
                bytes[o] = (byte) ((triple & (0xFF << bytePosition)) >>> bytePosition);
            }

        }

        return bytes;
    }

}
