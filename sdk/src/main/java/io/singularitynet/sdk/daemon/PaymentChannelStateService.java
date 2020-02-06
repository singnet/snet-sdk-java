package io.singularitynet.sdk.daemon;

import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.daemon.escrow.*;
import io.singularitynet.daemon.escrow.StateService.*;
import io.singularitynet.daemon.escrow.PaymentChannelStateServiceGrpc.*;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.Signature;
import io.singularitynet.sdk.mpe.MultiPartyEscrowContract;

public class PaymentChannelStateService {

    private final static Logger log = LoggerFactory.getLogger(PaymentChannelStateService.class);

    private final MessageSigningHelper signingHelper;
    private final PaymentChannelStateServiceBlockingStub stub;

    public PaymentChannelStateService(DaemonConnection daemonConnection,
            MultiPartyEscrowContract mpe, Ethereum ethereum, Identity signer) {
        this.signingHelper = new MessageSigningHelper(mpe.getContractAddress(), ethereum, signer);
        this.stub = daemonConnection.getGrpcStub(PaymentChannelStateServiceGrpc::newBlockingStub);
    }

    public PaymentChannelStateReply getChannelState(BigInteger channelId) {
        log.info("Requesting payment channel state from daemon");

        ChannelStateRequest.Builder request = ChannelStateRequest.newBuilder()
            .setChannelId(toBytesString(channelId));

        signingHelper.signChannelStateRequest(request); 

        ChannelStateReply grpcReply = stub.getChannelState(request.build());
        PaymentChannelStateReply.Builder builder = PaymentChannelStateReply.newBuilder()
            .setCurrentNonce(toBigInt(grpcReply.getCurrentNonce()));

        if (grpcReply.getCurrentSignedAmount() != ByteString.EMPTY) {
            builder.setCurrentSignedAmount(toBigInt(grpcReply.getCurrentSignedAmount()));
            builder.setCurrentSignature(new Signature(grpcReply.getCurrentSignature().toByteArray()));
        }

        PaymentChannelStateReply reply = builder.build();
        log.info("Payment channel state received: {}", reply);
        return reply;
    }

    private static ByteString toBytesString(BigInteger value) {
        return ByteString.copyFrom(Utils.bigIntToBytes32(value));
    }

    private static BigInteger toBigInt(ByteString value) {
        return Utils.bytes32ToBigInt(value.toByteArray());
    }

    static class MessageSigningHelper {

        private static final byte[] GET_CHANNEL_STATE_PREFIX = Utils.strToBytes("__get_channel_state");

        private final byte[] mpeContractAddress;
        private final Ethereum ethereum;
        private final Identity signer;

        public MessageSigningHelper(Address mpeAddress, Ethereum ethereum,
                Identity signer) {
            this.mpeContractAddress = mpeAddress.toByteArray();
            this.ethereum = ethereum;
            this.signer = signer;
        }

        public void signChannelStateRequest(ChannelStateRequest.Builder request) {
            Utils.wrapExceptions(() -> {
                long block = ethereum.getEthBlockNumber().longValue();

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bytes.write(GET_CHANNEL_STATE_PREFIX);
                bytes.write(mpeContractAddress);
                bytes.write(request.getChannelId().toByteArray());
                bytes.write(Utils.bigIntToBytes32(BigInteger.valueOf(block)));

                return request
                    .setCurrentBlock(block)
                    .setSignature(ByteString.copyFrom(signer.sign(bytes.toByteArray()).getBytes()));
            });
        }

    }

}
