package io.singularitynet.sdk.client;

import lombok.ToString;

import io.singularitynet.sdk.mpe.PaymentChannel;

@ToString
public class UpdatePaymentChannelPaymentStrategy extends PaymentChannelPaymentStrategy {
        
    public UpdatePaymentChannelPaymentStrategy() {
    }

    @Override
    protected PaymentChannel selectChannel(ServiceClient serviceClient) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
