package io.singularitynet.sdk.mpe;

import java.util.Optional;
import java.math.BigInteger;
import org.web3j.tuples.generated.Tuple7;

import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.registry.Utils;

public class MultiPartyEscrowContract {

    private final MultiPartyEscrow mpe;

    public MultiPartyEscrowContract(MultiPartyEscrow mpe) {
        this.mpe = mpe;
    }

    public Optional<PaymentChannel> getChannelById(BigInteger channelId) {
        return Utils.wrapExceptions(() -> {
            // TODO: test what contract returns on non-existing channel id
            Tuple7<BigInteger, String, String, String, byte[], BigInteger, BigInteger> channel =
                mpe.channels(channelId).send();
            return Optional.of(PaymentChannel.newBuilder()
                    .setChannelId(channelId)
                    .setMpeContractAddress(mpe.getContractAddress())
                    .setNonce(channel.component1())
                    .setSender(channel.component2())
                    .setSigner(channel.component3())
                    .setRecipient(channel.component4())
                    .setPaymentGroupId(channel.component5())
                    .setValue(channel.component6())
                    .setExpiration(channel.component7())
                    .build());
        });
    }

}
