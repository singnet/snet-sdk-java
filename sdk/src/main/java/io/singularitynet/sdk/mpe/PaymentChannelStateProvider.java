package io.singularitynet.sdk.mpe;

import java.math.BigInteger;

/**
 * This interface is responsible for providing up-to-date payment channel
 * states to a caller. Its methods return actual channel state which can be
 * different from blockchain state returned by MultiPartyEscrow contract. It
 * can be done by various different ways. The straightforward way is calling
 * daemon each time to get the state. Another way is calculating number of
 * successful and error calls to predict channel state.
 * @see io.singularitynet.sdk.mpe.PaymentChannelManager
 */
public interface PaymentChannelStateProvider {
    
    /**
     * Get single channel state by channel id. 
     * @param channelId id of the channel to retrieve the state.
     * @return actual payment channel state.
     */
    PaymentChannel getChannelStateById(BigInteger channelId);

}
