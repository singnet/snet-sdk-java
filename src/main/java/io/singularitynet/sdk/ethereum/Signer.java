package io.singularitynet.sdk.ethereum;

public interface Signer {

    byte[] sign(byte[] message);

}
