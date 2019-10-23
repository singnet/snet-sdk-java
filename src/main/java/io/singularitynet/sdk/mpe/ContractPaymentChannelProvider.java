package io.singularitynet.sdk.mpe;

import java.math.BigInteger;

public class ContractPaymentChannelProvider implements PaymentChannelProvider {

    private final MultiPartyEscrowContract mpe;

    public ContractPaymentChannelProvider(MultiPartyEscrowContract mpe) {
        this.mpe = mpe;
    }

    @Override
    public PaymentChannel getChannelById(BigInteger channelId) {
        return mpe.getChannelById(channelId).get();
    }

}
