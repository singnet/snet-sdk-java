package io.singularitynet.sdk.client;

import java.math.BigInteger;
import lombok.ToString;

import io.singularitynet.sdk.mpe.PaymentChannel;

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
        PaymentChannel channel = serviceClient.getPaymentChannelManager()
            .getChannelStateById(channelId);
        return channel;
    }

}
