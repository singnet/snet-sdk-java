package io.singularitynet.sdk.mpe;

import java.util.Optional;
import java.math.BigInteger;
import org.web3j.tuples.generated.Tuple7;

import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;

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
                    .setMpeContractAddress(getContractAddress())
                    .setNonce(channel.component1())
                    .setSender(new Address(channel.component2()))
                    .setSigner(new Address(channel.component3()))
                    .setRecipient(new Address(channel.component4()))
                    .setPaymentGroupId(channel.component5())
                    .setValue(channel.component6())
                    .setExpiration(channel.component7())
                    .setSpentAmount(BigInteger.ZERO)
                    .build());
        });
    }

    public Address getContractAddress() {
        return new Address(mpe.getContractAddress());
    }

}
