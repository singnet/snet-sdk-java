package io.singularitynet.sdk.daemon;

import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import com.google.protobuf.ByteString;
import org.web3j.protocol.core.Ethereum;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import io.singularitynet.daemon.escrow.*;
import io.singularitynet.daemon.escrow.StateService.*;
import io.singularitynet.daemon.escrow.PaymentChannelStateServiceGrpc.*;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Signer;
import io.singularitynet.sdk.mpe.PaymentChannel;

public class PaymentChannelStateService {

    private final MessageSigningHelper signingHelper;
    private final PaymentChannelStateServiceBlockingStub stub;

    public PaymentChannelStateService(DaemonConnection daemonConnection,
            Ethereum ethereum, Signer signer) {
        this.signingHelper = new MessageSigningHelper(ethereum, signer);
        this.stub = daemonConnection.getGrpcStub(PaymentChannelStateServiceGrpc::newBlockingStub);
    }

    public PaymentChannelStateReply getChannelState(PaymentChannel channel) {
        ChannelStateRequest.Builder request = ChannelStateRequest.newBuilder()
            // TODO: make MPE contract address be a part of channel id
            .setChannelId(toBytesString(channel.getChannelId()));

        signingHelper.signChannelStateRequest(channel, request); 

        ChannelStateReply grpcReply = stub.getChannelState(request.build());

        PaymentChannelStateReply.Builder reply = PaymentChannelStateReply.newBuilder()
            .setCurrentNonce(toBigInt(grpcReply.getCurrentNonce()));

        if (grpcReply.getCurrentSignedAmount() == ByteString.EMPTY) {
            return reply.build();
        }

        reply.setCurrentSignedAmount(toBigInt(grpcReply.getCurrentSignedAmount()));
        reply.setCurrentSignature(grpcReply.getCurrentSignature().toByteArray());

        return reply.build();
    }

    private static ByteString toBytesString(BigInteger value) {
        return ByteString.copyFrom(Utils.bigIntToBytes32(value));
    }

    private static BigInteger toBigInt(ByteString value) {
        return Utils.bytes32ToBigInt(value.toByteArray());
    }

    static class MessageSigningHelper {

        private static final byte[] GET_CHANNEL_STATE_PREFIX = Utils.strToBytes("__get_channel_state");

        private final Ethereum ethereum;
        private final Signer signer;

        public MessageSigningHelper(Ethereum ethereum, Signer signer) {
            this.ethereum = ethereum;
            this.signer = signer;
        }

        public void signChannelStateRequest(PaymentChannel channel, ChannelStateRequest.Builder request) {
            Utils.wrapExceptions(() -> {
                long block = ethereum.ethBlockNumber().send().getBlockNumber().longValue();

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bytes.write(GET_CHANNEL_STATE_PREFIX);
                bytes.write(channel.getMpeContractAddress().toByteArray());
                bytes.write(request.getChannelId().toByteArray());
                bytes.write(Utils.bigIntToBytes32(BigInteger.valueOf(block)));

                return request
                    .setCurrentBlock(block)
                    .setSignature(ByteString.copyFrom(signer.sign(bytes.toByteArray())));
            });
        }

    }

}
