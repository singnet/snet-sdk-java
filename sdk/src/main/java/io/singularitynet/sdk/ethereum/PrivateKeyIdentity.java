package io.singularitynet.sdk.ethereum;

import java.math.BigInteger;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Credentials;

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
        return new Signature(signature);
    }

    @Override
    public Address getAddress() {
        return new Address(Keys.getAddress(key.getPublicKey()));
    }

    public Credentials getCredentials() {
        return Credentials.create(key);
    }

}
