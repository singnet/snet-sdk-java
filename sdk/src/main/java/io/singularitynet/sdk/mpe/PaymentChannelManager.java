package io.singularitynet.sdk.mpe;

import java.math.BigInteger;
import java.util.stream.Stream;

import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.WithAddress;
import io.singularitynet.sdk.registry.PaymentGroupId;

/**
 * This interface provides payment channel management methods for specific
 * organization.
 */
public interface PaymentChannelManager {

    /**
     * Get all channels for the given payment group accessible by the given
     * identity. Method returns a stream of opened payment channels.
     * @param paymentGroupId id of the payment group.
     * @param identity identity which is able to make payments using channel.
     * @return list of actual payment channel states.
     */
    Stream<PaymentChannel> getChannelsAccessibleBy(PaymentGroupId paymentGroupId,
            WithAddress identity);

    /**
     * Open channel for the given payment group of the organization.
     * @param paymentGroupId id of the payment group.
     * @param recipient recipient of the payment group.
     * @param signer identity which can be used to sign payments on this
     * channel.
     * @param value initial value in the channel in cogs.
     * @param expiration the block number at which channel is expired.
     * @return created payment channel.
     */
    PaymentChannel openPaymentChannel(PaymentGroupId paymentGroupId,
            Address recipient, WithAddress signer, BigInteger value,
            BigInteger expiration);

    /**
     * Add funds to the channel value.
     * @param channel payment channel to add funds.
     * @param amount number of cogs to add.
     * @return updated payment channel state.
     */
    PaymentChannel addFundsToChannel(PaymentChannel channel, BigInteger amount);

    /**
     * Extend channel expiration block. Expiration block can be increased
     * only.
     * @param channel payment channel to extend.
     * @param expiration new expiration block.
     * @return updated payment channel state.
     */
    PaymentChannel extendChannel(PaymentChannel channel, BigInteger expiration);

    /**
     * Extend channel expiration block and add funds to channel at once. This
     * operation is more effective then combination of extendChannel and
     * addFundsToChannel from ethereum gas perspective. It should be used when
     * both updates are required.
     * @param channel payment channel to update
     * @param expiration new expiration block, can be increased only.
     * @param amount number of cogs to add.
     * @return updated payment channel state.
     */
    PaymentChannel extendAndAddFundsToChannel(PaymentChannel channel,
            BigInteger expiration, BigInteger amount);

}
