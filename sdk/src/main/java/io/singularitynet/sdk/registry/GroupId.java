package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;

import io.singularitynet.sdk.common.Preconditions;
import io.singularitynet.sdk.common.Utils;

@EqualsAndHashCode
public class GroupId {

    String groupId;

    public GroupId(String groupId) {
        this.groupId = groupId;
    }

    public static GroupId fromBytes(byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 32, "Payment group id should be 32 bytes length");
        return new GroupId(Utils.bytesToBase64(bytes));
    }

    public byte[] getBytes() {
        return Utils.base64ToBytes(groupId);
    }

    @Override
    public String toString() {
        return groupId;
    }

}
