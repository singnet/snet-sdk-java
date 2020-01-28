package io.singularitynet.sdk.mpe;

import java.util.Optional;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

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

    public PaymentChannel openChannel(Address signer, Address recipient,
            PaymentGroupId groupId, BigInteger value, BigInteger expiration) {
        return Utils.wrapExceptions(() -> {
            TransactionReceipt transaction = mpe.openChannel(signer.toString(),
                    recipient.toString(), groupId.getBytes(), value,
                    expiration).send();
            MultiPartyEscrow.ChannelOpenEventResponse event =
                mpe.getChannelOpenEvents(transaction).get(0);
            return PaymentChannel.newBuilder()
                .setChannelId(event.channelId)
                .setMpeContractAddress(getContractAddress())
                .setNonce(event.nonce)
                .setSender(new Address(event.sender))
                .setSigner(new Address(event.signer))
                .setRecipient(new Address(event.recipient))
                .setPaymentGroupId(new PaymentGroupId(event.groupId))
                .setValue(event.amount)
                .setExpiration(event.expiration)
                .setSpentAmount(BigInteger.ZERO)
                .build();
        });
    }

    public void transfer(Address receiver, BigInteger value) {
        Utils.wrapExceptions(() -> {
            mpe.transfer(receiver.toString(), value).send();
            return null;
        });
    }

}
