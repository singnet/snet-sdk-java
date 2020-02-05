package io.singularitynet.sdk.ethereum;

public interface Identity extends WithAddress {

    Signature sign(byte[] message);

}
