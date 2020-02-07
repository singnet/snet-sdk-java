package io.singularitynet.sdk.ethereum;

import java.math.BigInteger;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Credentials;

import io.singularitynet.sdk.common.Utils;

/**
 * Identity which private key is provided explicitly via constructor.
 */
public class PrivateKeyIdentity implements Identity {

    private final ECKeyPair key;

    /**
     * New identity from web3j specific key representation.
     * @param key web3j key pair.
     */
    public PrivateKeyIdentity(ECKeyPair key) {
        this.key = key;
    }

    /**
     * New identity from BigInteger.
     * @param privateKey private key packed into BigInteger.
     */
    public PrivateKeyIdentity(BigInteger privateKey) {
        this(ECKeyPair.create(privateKey));
    }

    /**
     * New identity from byte array.
     * @param privateKey private key packed into byte array.
     */
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

    // TODO: think about replacing it by getECKeyPair 
    /**
     * Return web3j credentials instance.
     * @return new web3j Credentials instance.
     */
    public Credentials getCredentials() {
        return Credentials.create(key);
    }

}
