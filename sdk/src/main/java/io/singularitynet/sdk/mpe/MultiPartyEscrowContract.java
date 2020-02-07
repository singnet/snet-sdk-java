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

/**
 * Class adapts web3j generated contract API to the SDK data structures and
 * calling conventions.
 */
public class MultiPartyEscrowContract {

    private final static Logger log = LoggerFactory.getLogger(MultiPartyEscrowContract.class);

    private final Web3j web3j;
    private final MultiPartyEscrow mpe;

    /**
     * New adapter from web3j generated contract.
     * @param web3j web3j instance.
     * @param mpe MultiPartyEscrow generated contract.
     */
    public MultiPartyEscrowContract(Web3j web3j, MultiPartyEscrow mpe) {
        this.web3j = web3j;
        this.mpe = mpe;
    }

    /**
     * Get channel state from Ethereum blockchain by channel id.
     * @param channelId channel id.
     * return blockchain payment channel state.
     */
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

    /**
     * Get MultiPartyEscrow contract address.
     * @return contract address.
     */
    public Address getContractAddress() {
        return new Address(mpe.getContractAddress());
    }

    /**
     * Open MultiPartyEscrow channel.
     * @param signer address of the identity which will be able to sign checks
     * in this channel.
     * @param recipient checks recipient.
     * @see io.singularitynet.sdk.registry.PaymentDetails#getPaymentAddress
     * @param groupId payment group id.
     * @see io.singularitynet.sdk.registry.PaymentGroup#getPaymentGroupId
     * @param value number of cogs to add into channel.
     * @param expiration payment channel expiration time in Ethereum blocks.
     * Method adds current Ethereum block plus 1 to this value to guarantee
     * that it is later then the next transaction block.
     * @return opened payment channel data.
     */
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

    /**
     * Transfer AGI tokens to another address within MultiPartyEscrow contract.
     * @param receiver target address.
     * @param value number of cogs to transfer.
     */
    public void transfer(Address receiver, BigInteger value) {
        Utils.wrapExceptions(() -> {
            mpe.transfer(receiver.toString(), value).send();
            return null;
        });
    }

    // TODO: use server side filtering to restrict number of channels
    /**
     * Return stream of the channel open events up to the latest block.
     * @return payment channel stream. Elements of the stream contains channel
     * state on the moment of the channel opening. It doesn't contain later
     * channel modifications.
     * @see io.singularitynet.sdk.mpe.MultiPartyEscrowContract#getChannelById
     */
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

    /**
     * Add funds to the payment channel.
     * @param channelId id of the channel to be updated.
     * @param amount number of cogs to add.
     * @return number of cogs added.
     */
    public BigInteger channelAddFunds(BigInteger channelId, BigInteger amount) {
        return Utils.wrapExceptions(() -> {
            TransactionReceipt transaction = mpe.channelAddFunds(
                    channelId, amount).send();
            MultiPartyEscrow.ChannelAddFundsEventResponse event =
                mpe.getChannelAddFundsEvents(transaction).get(0);
            return event.additionalFunds;
        });
    }

    /**
     * Extend payment channel expiration date.
     * @param channelId id of the channel to update.
     * @param expiration new expiration block. Should be later than current
     * expiration block.  Method adds current Ethereum block plus 1 to this
     * value to guarantee that it is later then the next transaction block.
     * @return new expiration block.
     */
    public BigInteger channelExtend(BigInteger channelId, BigInteger expiration) {
        return Utils.wrapExceptions(() -> {
            TransactionReceipt transaction = mpe.channelExtend(
                    channelId, shiftToNextBlock(expiration)).send();
            MultiPartyEscrow.ChannelExtendEventResponse event =
                mpe.getChannelExtendEvents(transaction).get(0);
            return event.newExpiration;
        });
    }

    /**
     * Pair of number of cogs added and expiration block.
     */
    public static class ExtendAndAddFundsResponse {

        /**
         * Expiration block.
         */
        public final BigInteger expiration;

        /**
         * Number of cogs added.
         */
        public final BigInteger valueIncrement;

        ExtendAndAddFundsResponse(BigInteger expiration,
                BigInteger valueIncrement) {
            this.expiration = expiration;
            this.valueIncrement = valueIncrement;
        }
    }

    /**
     * Extend expiration time and add funds to the channel in same operation.
     * This operation can be more gas efficient if you need doing both updates.
     * @param channelId id of the channel to be updated.
     * @param expiration new expiration block. Should be later than current
     * expiration block.  Method adds current Ethereum block plus 1 to this
     * value to guarantee that it is later then the next transaction block.
     * @return pair of number of cogs added and new expiration block.
     */
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
