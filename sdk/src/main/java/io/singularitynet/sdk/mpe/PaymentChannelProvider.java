package io.singularitynet.sdk.mpe;

import java.math.BigInteger;
import java.util.stream.Stream;

import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.registry.PaymentGroupId;

/**
 * This interface is responsible for providing up-to-date payment channel
 * states to a caller. Its methods return actual channel state which can be
 * different from blockchain state returned by MultiPartyEscrow contract. It
 * can be done by various different ways. The straightforward way is calling
 * daemon each time to get the state. Another way is calculating number of
 * successful and error calls to predict channel state.
 */
public interface PaymentChannelProvider {

    /**
     * Get single channel state by channel id. 
     * @param channelId id of the channel to retrieve the state.
     * @return actual payment channel state.
     */
    PaymentChannel getChannelById(BigInteger channelId);

    // TODO: should we use default signer in PaymentChannelProvider like in
    // MetadataProvider?
    /**
     * Get all channels which are available to given signer. Method returns
     * stream of payment channel which can be used to sign payments using
     * passed signer identity.
     * @param signer identity which is expected to have access to the returned
     * channels.
     * @return list of actual payment channel states.
     */
    Stream<PaymentChannel> getAllChannels(Address signer);

}
