package io.singularitynet.sdk.mpe;

import java.math.BigInteger;
import static com.google.common.base.Preconditions.checkState;

import io.singularitynet.sdk.daemon.PaymentChannelStateReply;
import io.singularitynet.sdk.daemon.PaymentChannelStateService;
import io.singularitynet.sdk.ethereum.CryptoUtils;

public class AskDaemonFirstPaymentChannelProvider implements PaymentChannelProvider {

    private final MultiPartyEscrowContract mpe;
    private final PaymentChannelStateService stateService;

    public AskDaemonFirstPaymentChannelProvider(MultiPartyEscrowContract mpe,
            PaymentChannelStateService stateService) {
        this.mpe = mpe;
        this.stateService = stateService;
    }

    @Override
    public PaymentChannel getChannelById(BigInteger channelId) {
        PaymentChannel channel = mpe.getChannelById(channelId).get();
        PaymentChannelStateReply reply = stateService.getChannelState(channel);
        if (!reply.hasCurrentSignedAmount()) {
            checkState(channel.getNonce().compareTo(reply.getCurrentNonce()) >= 0,
                    "Daemon sent channel state which is newer then blockchain one. " +
                    "Channel id: %s", channel.getChannelId());
            return channel;
        }
        return mergeChannelState(channel, reply);
    }

    private static final BigInteger ONE = BigInteger.valueOf(1);

    private static PaymentChannel mergeChannelState(PaymentChannel channel,
            PaymentChannelStateReply daemonState) {

        BigInteger spentAmount;

        if (channel.getNonce().equals(daemonState.getCurrentNonce())) {
            verifySignature(channel, daemonState.getCurrentSignedAmount(),
                    daemonState.getCurrentSignature(), "last current nonce");
            spentAmount = daemonState.getCurrentSignedAmount();
        } else {
            // TODO: test this case
            checkState(daemonState.getCurrentNonce().subtract(channel.getNonce())
                    .equals(ONE), "Difference between current channel nonce " +
                    "and daemon channel nonce is bigger than 1. Channel id: %s",
                    channel.getChannelId());
            verifySignature(channel, daemonState.getOldNonceSignedAmount(),
                    daemonState.getOldNonceSignature(), "last old nonce");
            verifySignature(channel.toBuilder().setNonce(daemonState.getCurrentNonce()).build(),
                    daemonState.getCurrentSignedAmount(),
                    daemonState.getCurrentSignature(), "last current nonce");
            spentAmount = daemonState.getCurrentSignedAmount().add(
                    daemonState.getOldNonceSignedAmount());
        }

        return channel.toBuilder()
            .setSpentAmount(daemonState.getCurrentSignedAmount())
            .build();
    }

    private static void verifySignature(PaymentChannel channel, BigInteger amount,
            byte[] signature, String type) {
        byte[] payment = EscrowPayment.newBuilder()
            .setPaymentChannel(channel)
            .setAmount(amount)
            .getMessage();
        String address = CryptoUtils.getSignerAddress(payment, signature);
        checkState(channel.getSigner().equals(address) ||
                channel.getSender().equals(address), 
                "Signature signer is not sender not signer. " + 
                "Daemon returned incorrect signature of the %s payment. " +
                "Channel id: %s", type, channel.getChannelId());
    }

}
