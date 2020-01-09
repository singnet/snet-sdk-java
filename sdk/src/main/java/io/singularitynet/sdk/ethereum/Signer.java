package io.singularitynet.sdk.ethereum;

public interface Signer extends WithAddress {

    Signature sign(byte[] message);

}
