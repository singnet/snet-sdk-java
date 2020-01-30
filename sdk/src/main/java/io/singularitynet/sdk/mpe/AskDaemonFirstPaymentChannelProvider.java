package io.singularitynet.sdk.mpe;

import java.math.BigInteger;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.Ethereum;

import io.singularitynet.sdk.common.Preconditions;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.daemon.PaymentChannelStateReply;
import io.singularitynet.sdk.daemon.PaymentChannelStateService;
import io.singularitynet.sdk.ethereum.CryptoUtils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Signature;
import io.singularitynet.sdk.registry.PaymentGroupId;

public class AskDaemonFirstPaymentChannelProvider implements PaymentChannelProvider {

    private final static Logger log = LoggerFactory.getLogger(AskDaemonFirstPaymentChannelProvider.class);

    private final Ethereum ethereum;
    private final MultiPartyEscrowContract mpe;
    private final PaymentChannelStateService stateService;

    public AskDaemonFirstPaymentChannelProvider(Ethereum ethereum,
            MultiPartyEscrowContract mpe,
            PaymentChannelStateService stateService) {
        this.ethereum = ethereum;
        this.mpe = mpe;
        this.stateService = stateService;
    }

    @Override
    public PaymentChannel getChannelById(BigInteger channelId) {
        log.debug("Getting the channel state, channelId: {}", channelId);
        PaymentChannel channel = mpe.getChannelById(channelId).get();
        PaymentChannelStateReply reply = stateService.getChannelState(channelId);
        if (!reply.hasCurrentSignedAmount()) {
            log.info("No payments on the channel in the daemon");
            Preconditions.checkState(channel.getNonce().compareTo(reply.getCurrentNonce()) >= 0,
                    "Daemon sent channel state which is newer then blockchain one. " +
                    "Channel id: %s", channel.getChannelId());
        } else {
            channel = mergeChannelState(channel, reply);
        }
        log.debug("Channel state, channel: {}", channel);
        return channel;
    }

    @Override
    public Stream<PaymentChannel> getAllChannels(Address signer) {
        return mpe.getChannelOpenEvents()
            .filter(ch -> ch.getSender().equals(signer)
                    || ch.getSigner().equals(signer))
            .map(ch -> mpe.getChannelById(ch.getChannelId()).get());
    }

    @Override
    public PaymentChannel openChannel(Address signer, Address recipient,
            PaymentGroupId groupId, BigInteger value, BigInteger lifetimeInBlocks) {
        BigInteger currentBlock = Utils.wrapExceptions(() -> ethereum.ethBlockNumber().send().getBlockNumber());
        BigInteger expiration = currentBlock.add(lifetimeInBlocks);
        PaymentChannel channel = mpe.openChannel(signer, recipient, groupId,
                value, expiration);
        return channel;
    }

    private static final BigInteger ONE = BigInteger.valueOf(1);

    private static PaymentChannel mergeChannelState(PaymentChannel blockchainState,
            PaymentChannelStateReply daemonState) {

        BigInteger spentAmount;

        if (blockchainState.getNonce().equals(daemonState.getCurrentNonce())) {
            verifySignature(blockchainState, daemonState.getCurrentSignedAmount(),
                    daemonState.getCurrentSignature(), "last current nonce");
            spentAmount = daemonState.getCurrentSignedAmount();
        } else {
            log.info("The channel nonce is different for the daemon and blockchain, blockchainState: {}, daemonState: {}",
                    blockchainState, daemonState);
            // TODO: test this case
            Preconditions.checkState(daemonState.getCurrentNonce().subtract(blockchainState.getNonce())
                    .equals(ONE), "Difference between current channel nonce " +
                    "and daemon channel nonce is bigger than 1. Channel id: %s",
                    blockchainState.getChannelId());
            verifySignature(blockchainState, daemonState.getOldNonceSignedAmount(),
                    daemonState.getOldNonceSignature(), "last old nonce");
            verifySignature(blockchainState.toBuilder().setNonce(daemonState.getCurrentNonce()).build(),
                    daemonState.getCurrentSignedAmount(),
                    daemonState.getCurrentSignature(), "last current nonce");
            spentAmount = daemonState.getCurrentSignedAmount().add(
                    daemonState.getOldNonceSignedAmount());
        }

        return blockchainState.toBuilder()
            .setSpentAmount(daemonState.getCurrentSignedAmount())
            .build();
    }

    private static void verifySignature(PaymentChannel channel, BigInteger amount,
            Signature signature, String type) {
        byte[] payment = EscrowPayment.newBuilder()
            .setPaymentChannel(channel)
            .setAmount(amount)
            .getMessage();
        Address address = CryptoUtils.getSignerAddress(payment, signature);
        Preconditions.checkState(channel.getSigner().equals(address) ||
                channel.getSender().equals(address), 
                "Signature signer is not sender not signer. " + 
                "Daemon returned incorrect signature of the %s payment. " +
                "Channel: %s, Payment signer: %s", type, channel, address);
    }

}
