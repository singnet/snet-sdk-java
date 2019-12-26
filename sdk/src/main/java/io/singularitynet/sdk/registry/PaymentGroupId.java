package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;

import io.singularitynet.sdk.common.Preconditions;
import io.singularitynet.sdk.common.Utils;

@EqualsAndHashCode
public class PaymentGroupId {

    String groupId;

    public PaymentGroupId(String groupId) {
        this.groupId = groupId;
    }

    public PaymentGroupId(byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 32, "Payment group id should be 32 bytes length");
        this.groupId = Utils.bytesToBase64(bytes);
    }

    public byte[] getBytes() {
        return Utils.base64ToBytes(groupId);
    }

    @Override
    public String toString() {
        return groupId;
    }

}
