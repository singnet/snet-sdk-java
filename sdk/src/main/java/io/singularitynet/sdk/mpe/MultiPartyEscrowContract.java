package io.singularitynet.sdk.mpe;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Spliterators;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.Contract;

import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.registry.PaymentGroupId;

public class MultiPartyEscrowContract {

    private final static Logger log = LoggerFactory.getLogger(MultiPartyEscrowContract.class);

    private final Web3j web3j;
    private final MultiPartyEscrow mpe;

    public MultiPartyEscrowContract(Web3j web3j, MultiPartyEscrow mpe) {
        this.web3j = web3j;
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
                    shiftToNextBlock(expiration))
                .send();
            MultiPartyEscrow.ChannelOpenEventResponse event =
                mpe.getChannelOpenEvents(transaction).get(0);
            return channelOpenEventAsPaymentChannel(event);
        });
    }

    private BigInteger shiftToNextBlock(BigInteger expiration) {
        return Utils.wrapExceptions(() -> {
            BigInteger blockBeforeCall = Utils.wrapExceptions(() -> web3j.ethBlockNumber().send().getBlockNumber());
            return blockBeforeCall.add(expiration).add(BigInteger.valueOf(1));
        });
    }

    private PaymentChannel channelOpenEventAsPaymentChannel(MultiPartyEscrow.ChannelOpenEventResponse event) {
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
    }

    public void transfer(Address receiver, BigInteger value) {
        Utils.wrapExceptions(() -> {
            mpe.transfer(receiver.toString(), value).send();
            return null;
        });
    }

    // TODO: use server side filtering to restrict number of channels
    public Stream<PaymentChannel> getChannelOpenEvents() {
        return Utils.wrapExceptions(() -> {
            EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST,
                    DefaultBlockParameterName.LATEST, getContractAddress().toString());
            filter.addSingleTopic(EventEncoder.encode(MultiPartyEscrow.CHANNELOPEN_EVENT));

            return web3j.ethGetLogs(filter).send().getLogs().stream()
                .map(res -> (Log)res)
                .map(log -> Contract.staticExtractEventParameters(MultiPartyEscrow.CHANNELOPEN_EVENT, log))
                .map(eventValues -> {
                    return PaymentChannel.newBuilder()
                        .setChannelId((BigInteger) eventValues.getNonIndexedValues().get(0).getValue())
                        .setMpeContractAddress(getContractAddress())
                        .setNonce((BigInteger) eventValues.getNonIndexedValues().get(1).getValue())
                        .setSender(new Address((String) eventValues.getIndexedValues().get(0).getValue()))
                        .setSigner(new Address((String) eventValues.getNonIndexedValues().get(2).getValue()))
                        .setRecipient(new Address((String) eventValues.getIndexedValues().get(1).getValue()))
                        .setPaymentGroupId(new PaymentGroupId((byte[]) eventValues.getIndexedValues().get(2).getValue()))
                        .setValue((BigInteger) eventValues.getNonIndexedValues().get(3).getValue())
                        .setExpiration((BigInteger) eventValues.getNonIndexedValues().get(4).getValue())
                        .setSpentAmount(BigInteger.ZERO)
                        .build();
                });
        });
    }

    public BigInteger channelAddFunds(BigInteger channelId, BigInteger amount) {
        return Utils.wrapExceptions(() -> {
            TransactionReceipt transaction = mpe.channelAddFunds(
                    channelId, amount).send();
            MultiPartyEscrow.ChannelAddFundsEventResponse event =
                mpe.getChannelAddFundsEvents(transaction).get(0);
            return event.additionalFunds;
        });
    }

    public BigInteger channelExtend(BigInteger channelId, BigInteger expiration) {
        return Utils.wrapExceptions(() -> {
            TransactionReceipt transaction = mpe.channelExtend(
                    channelId, shiftToNextBlock(expiration)).send();
            MultiPartyEscrow.ChannelExtendEventResponse event =
                mpe.getChannelExtendEvents(transaction).get(0);
            return event.newExpiration;
        });
    }

    public static class ExtendAndAddFundsResponse {
        public final BigInteger expiration;
        public final BigInteger valueIncrement;

        ExtendAndAddFundsResponse(BigInteger expiration,
                BigInteger valueIncrement) {
            this.expiration = expiration;
            this.valueIncrement = valueIncrement;
        }
    }

    public ExtendAndAddFundsResponse channelExtendAndAddFunds(BigInteger channelId,
            BigInteger expiration, BigInteger amount) {
        return Utils.wrapExceptions(() -> {
            TransactionReceipt transaction = mpe.channelExtendAndAddFunds(
                    channelId, shiftToNextBlock(expiration), amount).send();
            MultiPartyEscrow.ChannelExtendEventResponse extendsEvent =
                mpe.getChannelExtendEvents(transaction).get(0);
            MultiPartyEscrow.ChannelAddFundsEventResponse addFundsEvent =
                mpe.getChannelAddFundsEvents(transaction).get(0);
            return new ExtendAndAddFundsResponse(
                    extendsEvent.newExpiration,
                    addFundsEvent.additionalFunds);
        });
    }

}
