package io.singularitynet.sdk.ethereum;

import static com.google.common.base.Preconditions.checkState;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Hash;

import io.singularitynet.sdk.common.Utils;

public class CryptoUtils {

    public static String getSignerAddress(byte[] message, byte[] signature) {
        return Utils.wrapExceptions(() -> {
            Sign.SignatureData signatureData = bytesToSignature(signature);
            BigInteger publicKey = Sign.signedPrefixedMessageToKey(Hash.sha3(message), signatureData);
            return Keys.getAddress(publicKey);
        });
    }

    private static Sign.SignatureData bytesToSignature(byte[] signature) {
        checkState(signature.length == 65, "Incorrect signature length: not equal to 65");
        byte[] r = new byte[32];
        byte[] s = new byte[32];
        byte[] v = new byte[1];
        return Utils.wrapExceptions(() -> {
            ByteArrayInputStream bytes = new ByteArrayInputStream(signature);
            bytes.read(r, 0, 32);
            bytes.read(s, 0, 32);
            bytes.read(v, 0, 1);
            return new Sign.SignatureData(v, r, s);
        });
    }
}