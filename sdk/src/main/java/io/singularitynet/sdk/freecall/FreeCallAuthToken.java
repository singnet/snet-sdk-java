package io.singularitynet.sdk.freecall;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigInteger;

@EqualsAndHashCode
@ToString
public class FreeCallAuthToken {

    private final String dappUserId;
    private final BigInteger expirationBlock;
    private final String token;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private FreeCallAuthToken(Builder builder) {
        this.dappUserId = builder.dappUserId;
        this.expirationBlock = builder.expirationBlock;
        this.token = builder.token;
    }

    public String getDappUserId() {
        return dappUserId;
    }

    public BigInteger getExpirationBlock() {
        return expirationBlock;
    }

    public String getToken() {
        return token;
    }

    public static class Builder {

        private String dappUserId;
        private BigInteger expirationBlock;
        private String token;

        private Builder() {
        }

        private Builder(FreeCallAuthToken object) {
            this.dappUserId = object.dappUserId;
            this.expirationBlock = object.expirationBlock;
            this.token = object.token;
        }

        public Builder setDappUserId(String dappUserId) {
            this.dappUserId = dappUserId;
            return this;
        }

        public String getDappUserId() {
            return dappUserId;
        }

        public Builder setExpirationBlock(BigInteger expirationBlock) {
            this.expirationBlock = expirationBlock;
            return this;
        }

        public Builder setExpirationBlock(long expirationBlock) {
            this.expirationBlock = BigInteger.valueOf(expirationBlock);
            return this;
        }

        public BigInteger getExpirationBlock() {
            return expirationBlock;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public String getToken() {
            return token;
        }

        public FreeCallAuthToken build() {
            return new FreeCallAuthToken(this);
        }
    }
}
