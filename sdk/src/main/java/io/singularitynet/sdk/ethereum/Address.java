package io.singularitynet.sdk.ethereum;

import lombok.EqualsAndHashCode;
import io.singularitynet.sdk.common.Preconditions;

import io.singularitynet.sdk.common.Utils;

/**
 * Ethereum address.
 */
@EqualsAndHashCode
public class Address {

    private final String address;

    /**
     * Instantiate new address from string.
     * @param address Ethereum address string representation, may be prefixed
     * by "0x".
     */
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

    /**
     * Get binary address representation.
     * @return binary address representation as a byte array.
     */
    public byte[] toByteArray() {
        return Utils.hexToBytes(address);
    }

}
