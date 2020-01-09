package io.singularitynet.sdk.ethereum;

import lombok.EqualsAndHashCode;
import org.web3j.crypto.Sign;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import io.singularitynet.sdk.common.Preconditions;
import io.singularitynet.sdk.common.Utils;

@EqualsAndHashCode
public class Signature {

    private final Sign.SignatureData data;

    public Signature(Sign.SignatureData signature) {
        this.data = signature;
    }

    public Signature(byte[] signature) {
        this.data = bytesToSignature(signature);
    }

    public Sign.SignatureData getSignatureData() {
        return data;
    }

    public byte[] getBytes() {
        return signatureToBytes(data);
    }

    @Override
    public String toString() {
        return Utils.bytesToBase64(getBytes());
    }

    private static byte[] signatureToBytes(Sign.SignatureData signature) {
        return Utils.wrapExceptions(() -> {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bytes.write(signature.getR());
            bytes.write(signature.getS());
            bytes.write(signature.getV());
            return bytes.toByteArray();
        });
    }

    private static Sign.SignatureData bytesToSignature(byte[] signature) {
        Preconditions.checkState(signature.length == 65, "Incorrect signature length: not equal to 65");
        byte[] r = new byte[32];
        byte[] s = new byte[32];
        byte[] v = new byte[1];
        return Utils.wrapExceptions(() -> {
            ByteArrayInputStream bytes = new ByteArrayInputStream(signature);
            bytes.read(r, 0, 32);
            bytes.read(s, 0, 32);
            bytes.read(v, 0, 1);
            return new Sign.SignatureData(v[0], r, s);
        });
    }

}
