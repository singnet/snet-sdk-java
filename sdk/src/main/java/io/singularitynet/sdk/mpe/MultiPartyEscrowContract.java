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
                    .setNonce(channel.getValue1())
                    .setSender(new Address(channel.getValue2()))
                    .setSigner(new Address(channel.getValue3()))
                    .setRecipient(new Address(channel.getValue4()))
                    .setPaymentGroupId(channel.getValue5())
                    .setValue(channel.getValue6())
                    .setExpiration(channel.getValue7())
                    .setSpentAmount(BigInteger.ZERO)
                    .build());
        });
    }

    public Address getContractAddress() {
        return new Address(mpe.getContractAddress());
    }

}
