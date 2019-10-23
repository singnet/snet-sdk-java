package io.singularitynet.sdk.ethereum;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Hash;
import java.io.ByteArrayOutputStream;
import static com.google.common.base.Preconditions.checkState;

import io.singularitynet.sdk.registry.Utils;

public class PrivateKeyIdentity implements Signer {

    private final ECKeyPair key;

    public PrivateKeyIdentity(byte[] privateKey) {
        this.key = ECKeyPair.create(privateKey);
    }

    @Override
    public byte[] sign(byte[] message) {
        return signatureAsBytes(Sign.signPrefixedMessage(Hash.sha3(message), key));
    }

    private static byte[] signatureAsBytes(Sign.SignatureData signature) {
        return Utils.wrapExceptions(() -> {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bytes.write(signature.getR());
            bytes.write(signature.getS());
            bytes.write(signature.getV());
            return bytes.toByteArray();
        });
    }

}
