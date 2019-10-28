package io.singularitynet.sdk.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigInteger;

@EqualsAndHashCode
@ToString
public class Pricing {

    private final PriceModel priceModel;
    private final BigInteger priceInCogs;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private Pricing(Builder builder) {
        this.priceModel = builder.priceModel;
        this.priceInCogs = builder.priceInCogs;
    }

    public PriceModel getPriceModel() {
        return priceModel;
    }

    public BigInteger getPriceInCogs() {
        return priceInCogs;
    }

    public static class Builder {

        private PriceModel priceModel;
        private BigInteger priceInCogs;

        private Builder() {
        }

        private Builder(Pricing object) {
            this.priceModel = object.priceModel;
            this.priceInCogs = object.priceInCogs;
        }

        public Builder setPriceModel(PriceModel priceModel) {
            this.priceModel = priceModel;
            return this;
        }

        // TODO: replace BigInteger by Price type
        public Builder setPriceInCogs(BigInteger priceInCogs) {
            this.priceInCogs = priceInCogs;
            return this;
        }

        public Pricing build() {
            return new Pricing(this);
        }
    }
}
