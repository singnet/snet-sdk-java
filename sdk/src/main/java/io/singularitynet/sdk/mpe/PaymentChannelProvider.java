package io.singularitynet.sdk.mpe;

import java.math.BigInteger;

public interface PaymentChannelProvider {

    PaymentChannel getChannelById(BigInteger channelId);

}
