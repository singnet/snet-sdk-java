package io.singularitynet.sdk.ethereum;

import lombok.EqualsAndHashCode;
import io.singularitynet.sdk.common.Preconditions;

import io.singularitynet.sdk.common.Utils;

@EqualsAndHashCode
public class Address {

    private final String address;

    public Address(String address) {
        if (address.startsWith("0x")) {
            Preconditions.checkArgument(address.length() == 42, "Address length is not equal to 40: %s", address);
            this.address = address.substring(2);
        } else {
            Preconditions.checkArgument(address.length() == 40, "Address length is not equal to 40: %s", address);
            this.address = address;
        }
    }

    @Override
    public String toString() {
        return "0x" + address;
    }

    public byte[] toByteArray() {
        return Utils.hexToBytes(address);
    }

}
