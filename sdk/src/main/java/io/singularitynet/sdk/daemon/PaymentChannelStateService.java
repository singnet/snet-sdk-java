package io.singularitynet.sdk.daemon;

import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.daemon.escrow.StateService.*;
import io.singularitynet.daemon.escrow.PaymentChannelStateServiceGrpc;
import io.singularitynet.daemon.escrow.PaymentChannelStateServiceGrpc.*;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.Signature;

public class PaymentChannelStateService {

    private final static Logger log = LoggerFactory.getLogger(PaymentChannelStateService.class);

    private final MessageSigningHelper signingHelper;
    private final PaymentChannelStateServiceBlockingStub stub;

    public PaymentChannelStateService(DaemonConnection daemonConnection,
            Address mpeAddress, Ethereum ethereum) {
        this.signingHelper = new MessageSigningHelper(mpeAddress, ethereum);
        this.stub = daemonConnection.getGrpcStub(PaymentChannelStateServiceGrpc::newBlockingStub);
    }

    public PaymentChannelStateReply getChannelState(BigInteger channelId, Identity signer) {
        log.info("Requesting payment channel state from daemon, channelId: {}, signer: {}", channelId, signer);

        ChannelStateRequest.Builder request = ChannelStateRequest.newBuilder()
            .setChannelId(GrpcUtils.toBytesString(channelId));

        signingHelper.signChannelStateRequest(request, signer); 

        ChannelStateReply grpcReply = stub.getChannelState(request.build());
        PaymentChannelStateReply.Builder builder = PaymentChannelStateReply.newBuilder()
            .setCurrentNonce(GrpcUtils.toBigInt(grpcReply.getCurrentNonce()));

        if (!grpcReply.getCurrentSignedAmount().isEmpty()) {
            builder.setCurrentSignedAmount(GrpcUtils.toBigInt(grpcReply.getCurrentSignedAmount()));
        }

        if (!grpcReply.getCurrentSignature().isEmpty()) {
            builder.setCurrentSignature(new Signature(grpcReply.getCurrentSignature().toByteArray()));
        }

        if (!grpcReply.getOldNonceSignedAmount().isEmpty()) {
            builder.setOldNonceSignedAmount(GrpcUtils.toBigInt(grpcReply.getOldNonceSignedAmount()));
        }

        if (!grpcReply.getOldNonceSignature().isEmpty()) {
            builder.setOldNonceSignature(new Signature(grpcReply.getOldNonceSignature().toByteArray()));
        }

        PaymentChannelStateReply reply = builder.build();
        log.info("Payment channel state received: {}", reply);
        return reply;
    }

    static class MessageSigningHelper {

        private static final byte[] GET_CHANNEL_STATE_PREFIX = Utils.strToBytes("__get_channel_state");

        private final byte[] mpeContractAddress;
        private final Ethereum ethereum;

        public MessageSigningHelper(Address mpeAddress, Ethereum ethereum) {
            this.mpeContractAddress = mpeAddress.toByteArray();
            this.ethereum = ethereum;
        }

        public void signChannelStateRequest(ChannelStateRequest.Builder request, Identity signer) {
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
