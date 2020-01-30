package io.singularitynet.sdk.client;

import java.math.BigInteger;
import lombok.ToString;

import io.singularitynet.sdk.mpe.PaymentChannel;

@ToString
public class OnDemandPaymentChannelPaymentStrategy extends PaymentChannelPaymentStrategy {

    private final BigInteger expirationAdvance;
    private final BigInteger callsAdvance;
        
    public OnDemandPaymentChannelPaymentStrategy() {
        this.expirationAdvance = BigInteger.valueOf(2);
        this.callsAdvance = BigInteger.valueOf(1);
    }

    @Override
    protected PaymentChannel selectChannel(ServiceClient serviceClient) {
        return serviceClient.openPaymentChannel(
                serviceClient.getSigner(),
                ServiceClient.callsByFixedPrice(callsAdvance),
                ServiceClient.blocksAfterThreshold(expirationAdvance));
    }

}
