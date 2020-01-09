package io.singularitynet.sdk.mpe;

import java.util.Optional;
import java.math.BigInteger;
import org.web3j.tuples.generated.Tuple7;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.registry.PaymentGroupId;

public class MultiPartyEscrowContract {

    private final static Logger log = LoggerFactory.getLogger(MultiPartyEscrowContract.class);

    private final MultiPartyEscrow mpe;

    public MultiPartyEscrowContract(MultiPartyEscrow mpe) {
        this.mpe = mpe;
    }

    public Optional<PaymentChannel> getChannelById(BigInteger channelId) {
        return Utils.wrapExceptions(() -> {
            log.info("Get channel state from MultiPartyEscrow, channelId: {}", channelId);
            // TODO: test what contract returns on non-existing channel id
            Tuple7<BigInteger, String, String, String, byte[], BigInteger, BigInteger> result =
                mpe.channels(channelId).send();
            PaymentChannel channel = PaymentChannel.newBuilder()
                .setChannelId(channelId)
                .setMpeContractAddress(getContractAddress())
                .setNonce(result.getValue1())
                .setSender(new Address(result.getValue2()))
                .setSigner(new Address(result.getValue3()))
                .setRecipient(new Address(result.getValue4()))
                .setPaymentGroupId(new PaymentGroupId(result.getValue5()))
                .setValue(result.getValue6())
                .setExpiration(result.getValue7())
                .setSpentAmount(BigInteger.ZERO)
                .build();
            log.info("Channel state received: {}", channel);
            return Optional.of(channel);
        });
    }

    public Address getContractAddress() {
        return new Address(mpe.getContractAddress());
    }

}
