package io.singularitynet.sdk.ethereum;

import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Hash;
import static io.singularitynet.sdk.common.Preconditions.checkState;

import io.singularitynet.sdk.common.Utils;

public class PrivateKeyIdentity implements Signer {

    private final ECKeyPair key;

    public PrivateKeyIdentity(ECKeyPair key) {
        this.key = key;
    }

    public PrivateKeyIdentity(BigInteger privateKey) {
        this(ECKeyPair.create(privateKey));
    }

    public PrivateKeyIdentity(byte[] privateKey) {
        this(ECKeyPair.create(privateKey));
    }

    @Override
    public Signature sign(byte[] message) {
        Sign.SignatureData signature = Sign.signPrefixedMessage(Hash.sha3(message), key);
        return new Signature(signatureToBytes(signature));
    }

    @Override
    public Address getAddress() {
        return new Address(Keys.getAddress(key.getPublicKey()));
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

}
