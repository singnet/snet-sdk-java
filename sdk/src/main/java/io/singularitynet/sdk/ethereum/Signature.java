package io.singularitynet.sdk.ethereum;

import lombok.EqualsAndHashCode;

import io.singularitynet.sdk.common.Utils;

@EqualsAndHashCode
public class Signature {

    private final byte[] signature;

    public Signature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getBytes() {
        return signature;
    }

    @Override
    public String toString() {
        return Utils.bytesToBase64(signature);
    }

}
