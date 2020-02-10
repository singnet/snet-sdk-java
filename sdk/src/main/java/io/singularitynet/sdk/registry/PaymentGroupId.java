package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;

import io.singularitynet.sdk.common.Preconditions;
import io.singularitynet.sdk.common.Utils;

/**
 * Payment group id.
 */
@EqualsAndHashCode
public class PaymentGroupId {

    private final String groupId;

    /**
     * New payment group id from string representation.
     * @param groupId group id data encoded by base64.
     */
    public PaymentGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * New payment group id from bytes.
     * @param bytes group id in bytes.
     */
    public PaymentGroupId(byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 32, "Payment group id should be 32 bytes length");
        this.groupId = Utils.bytesToBase64(bytes);
    }

    /**
     * Return group id data as byte array.
     * @return byte array.
     */
    public byte[] getBytes() {
        return Utils.base64ToBytes(groupId);
    }

    @Override
    public String toString() {
        return groupId;
    }

}
