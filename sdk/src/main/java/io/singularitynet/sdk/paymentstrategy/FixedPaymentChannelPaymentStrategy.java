package io.singularitynet.sdk.paymentstrategy;

import java.math.BigInteger;
import lombok.ToString;

import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.client.ServiceClient;

/**
 * The class is responsible for providing a payment for the client call using
 * the specified payment channel.
 */
@ToString
public class FixedPaymentChannelPaymentStrategy extends EscrowPaymentStrategy {
        
    private final BigInteger channelId;

    /**
     * Constructor.
     * @param channelId id of the payment channel to use for the payment
     * generation.
     */
    public FixedPaymentChannelPaymentStrategy(BigInteger channelId) {
        this.channelId = channelId;
    }

    @Override
    protected PaymentChannel selectChannel(ServiceClient serviceClient) {
        PaymentChannel channel = serviceClient.getPaymentChannelStateProvider()
            .getChannelStateById(channelId, serviceClient.getSdk().getIdentity());
        return channel;
    }

}
