package io.singularitynet.sdk.daemon;

import java.util.Map;
import java.util.HashMap;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import com.google.protobuf.ByteString;

import io.singularitynet.daemon.escrow.*;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.payment.Payment;
import io.singularitynet.sdk.payment.PaymentSerializer;
import io.singularitynet.sdk.mpe.PaymentChannel;

public class DaemonMock extends PaymentChannelStateServiceGrpc.PaymentChannelStateServiceImplBase
    implements ServerInterceptor {

    private final List<Payment> payments = Collections.synchronizedList(new ArrayList<>());
    private final Map<BigInteger, StateService.ChannelStateReply> channelStates = new HashMap<>();

    @Override
    public <ReqT,RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT,RespT> call, Metadata headers,
            ServerCallHandler<ReqT,RespT> next) {
        Optional<Payment> payment = PaymentSerializer.fromMetadata(headers);
        if (payment.isPresent()) {
            payments.add(payment.get());
        }
        return next.startCall(call, headers);
    }

    public List<Payment> getPayments() {
        return payments;
    }

    @Override
    public void getChannelState(StateService.ChannelStateRequest request,
            StreamObserver<StateService.ChannelStateReply> callback) {
        BigInteger channelId = Utils.bytes32ToBigInt(request.getChannelId().toByteArray());
        StateService.ChannelStateReply reply = channelStates.get(channelId);
        if (reply == null) {
            callback.onError(new Throwable("No such channel"));
        }

        callback.onNext(reply);
        callback.onCompleted();
    }

    public void setChannelState(BigInteger channelId, PaymentChannelStateReply reply) {
        channelStates.put(channelId, StateService.ChannelStateReply.newBuilder()
                .setCurrentNonce(ByteString.copyFrom(Utils.bigIntToBytes32(reply.getCurrentNonce())))
                .setCurrentSignedAmount(ByteString.copyFrom(Utils.bigIntToBytes32(reply.getCurrentSignedAmount())))
                .setCurrentSignature(ByteString.copyFrom(reply.getCurrentSignature().getBytes()))
                .build());
    }

    public void setChannelStateIsAbsent(PaymentChannel channel) {
        channelStates.put(channel.getChannelId(), StateService.ChannelStateReply.newBuilder()
                .setCurrentNonce(ByteString.copyFrom(Utils.bigIntToBytes32(channel.getNonce())))
                .build());
    }

}
