package io.singularitynet.sdk.ethereum;

import java.math.BigInteger;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Hash;

import io.singularitynet.sdk.common.Utils;

/**
 * Crypto utility class
 */
public class CryptoUtils {

    private CryptoUtils() {
    }

    /**
     * Extract signer address from the signature.
     * @param message Etherum message signed.
     * @param signature message signature.
     * @return signer's Ethereum address.
     */
    public static Address getSignerAddress(byte[] message, Signature signature) {
        return Utils.wrapExceptions(() -> {
            Sign.SignatureData signatureData = signature.getSignatureData();
            BigInteger publicKey = Sign.signedPrefixedMessageToKey(Hash.sha3(message), signatureData);
            return new Address(Keys.getAddress(publicKey));
        });
    }

}
