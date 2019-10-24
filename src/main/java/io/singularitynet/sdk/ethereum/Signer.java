package io.singularitynet.sdk.ethereum;

public interface Signer extends WithAddress {

    byte[] sign(byte[] message);

}
