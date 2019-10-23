package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.google.gson.annotations.SerializedName;
import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode
@ToString
public class PaymentGroup {

    private final String groupName;
    @SerializedName("group_id") private final String paymentGroupId;
    @SerializedName("payment") private final PaymentDetails paymentDetails;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private PaymentGroup(Builder builder) {
        this.groupName = builder.groupName;
        this.paymentGroupId = builder.paymentGroupId;
        this.paymentDetails = builder.paymentDetails;
    }

    public String getGroupName() {
        return groupName;
    }

    public byte[] getPaymentGroupId() {
        return Utils.base64ToBytes(paymentGroupId);
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public static class Builder {

        private String groupName;
        private String paymentGroupId;
        private PaymentDetails paymentDetails;

        private Builder() {
        }

        private Builder(PaymentGroup object) {
            this.groupName = object.groupName;
            this.paymentGroupId = object.paymentGroupId;
            this.paymentDetails = object.paymentDetails;
        }

        public Builder setGroupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder setPaymentGroupId(byte[] paymentGroupId) {
            checkArgument(paymentGroupId.length == 32, "Payment group id should be 32 bytes length");
            this.paymentGroupId = Utils.bytesToBase64(paymentGroupId);
            return this;
        }

        public Builder setPaymentDetails(PaymentDetails paymentDetails) {
            this.paymentDetails = paymentDetails;
            return this;
        }

        public PaymentGroup build() {
            return new PaymentGroup(this);
        }
    }
}
