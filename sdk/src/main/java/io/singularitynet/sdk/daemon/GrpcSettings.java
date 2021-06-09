package io.singularitynet.sdk.daemon;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class GrpcSettings {

    public final static GrpcSettings DEFAULT = GrpcSettings.newBuilder()
        .setMaxInboundMessageSize(1 << 24) /* 16 Mb */
        .build();

    private final int maxInboundMessageSize;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private GrpcSettings(Builder builder) {
        this.maxInboundMessageSize = builder.maxInboundMessageSize;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public static class Builder {

        private int maxInboundMessageSize;

        private Builder() {
        }

        private Builder(GrpcSettings object) {
            this.maxInboundMessageSize = object.maxInboundMessageSize;
        }

        public Builder setMaxInboundMessageSize(int maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
            return this;
        }

        public int getMaxInboundMessageSize() {
            return maxInboundMessageSize;
        }

        public GrpcSettings build() {
            return new GrpcSettings(this);
        }
    }
}
