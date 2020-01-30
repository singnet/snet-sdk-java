package io.singularitynet.sdk.mpe;

import java.math.BigInteger;
import java.util.stream.Stream;

import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.registry.PaymentGroupId;

public interface PaymentChannelProvider {

    PaymentChannel getChannelById(BigInteger channelId);
    // TODO: should we use default signer in PaymentChannelProvider like in
    // MetadataProvider?
    Stream<PaymentChannel> getAllChannels(Address signer);
    PaymentChannel openChannel(Address signer, Address recipient,
            PaymentGroupId groupId, BigInteger value, BigInteger lifetimeInBlocks);

}
