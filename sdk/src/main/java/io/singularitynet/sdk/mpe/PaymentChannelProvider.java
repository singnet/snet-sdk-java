package io.singularitynet.sdk.mpe;

import java.math.BigInteger;

import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.registry.PaymentGroupId;

public interface PaymentChannelProvider {

    PaymentChannel getChannelById(BigInteger channelId);
    PaymentChannel openChannel(Address signer, Address recipient,
            PaymentGroupId groupId, BigInteger value, BigInteger lifetimeInBlocks);

}
