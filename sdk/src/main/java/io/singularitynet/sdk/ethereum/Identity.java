package io.singularitynet.sdk.ethereum;

/**
 * The interface represents Ethereum identity which owns private key and signs
 * messages. This interface may represent identities which delegates signing to
 * the external node via RPC or hardware device.
 */
public interface Identity extends WithAddress {

    /**
     * Sign message using private key.
     * @param message message to sign.
     * @return resulting signature.
     */
    Signature sign(byte[] message);

}
