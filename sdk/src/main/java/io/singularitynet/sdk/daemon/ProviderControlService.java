package io.singularitynet.sdk.daemon;

import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.stream.Stream;

import io.singularitynet.daemon.escrow.*;
import io.singularitynet.daemon.escrow.ProviderControlServiceGrpc.*;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.Signature;
import io.singularitynet.sdk.ethereum.Ethereum;

public class ProviderControlService {

    private final ProviderControlServiceBlockingStub stub;
    private final Address mpeContractAddress;
    private final Identity signer;
    private final Ethereum ethereum;

    public ProviderControlService(DaemonConnection daemonConnection,
            Address mpeContractAddress, Identity signer, Ethereum ethereum) {

        this.stub = daemonConnection.getGrpcStub(ProviderControlServiceGrpc::newBlockingStub);
        this.mpeContractAddress = mpeContractAddress;
        this.signer = signer;
        this.ethereum = ethereum;
    }

    public Stream<PaymentReply> getListUnclaimed() {
        BigInteger currentBlock = ethereum.getEthBlockNumber();

        Signature signature = MessageSigningHelper.signGetListUnclaimedRequest(
                mpeContractAddress, currentBlock, signer);

        ControlService.GetPaymentsListRequest request = ControlService.GetPaymentsListRequest.newBuilder()
            .setMpeAddress(mpeContractAddress.toString())
            .setCurrentBlock(currentBlock.longValue())
            .setSignature(ByteString.copyFrom(signature.getBytes()))
            .build();

        ControlService.PaymentsListReply reply = stub.getListUnclaimed(request);

        return reply.getPaymentsList().stream()
            .map(ProviderControlService::asPaymentReply);
    }

    public PaymentReply startClaim(BigInteger channelId) {
        PaymentReply state = getListUnclaimed()
            .filter(payment -> payment.getChannelId().equals(channelId))
            .findFirst().get();

        Signature signature = MessageSigningHelper.signStartClaimRequest(
                channelId, mpeContractAddress, state.getChannelNonce(), signer);

        ControlService.StartClaimRequest request = ControlService.StartClaimRequest.newBuilder()
            .setMpeAddress(mpeContractAddress.toString())
            .setChannelId(toBytesString(channelId))
            .setSignature(ByteString.copyFrom(signature.getBytes()))
            .build();

        ControlService.PaymentReply reply = stub.startClaim(request);

        return asPaymentReply(reply);
    }

    private static PaymentReply asPaymentReply(ControlService.PaymentReply reply) {
        PaymentReply.Builder builder = PaymentReply.newBuilder()
            .setChannelId(toBigInt(reply.getChannelId()))
            .setChannelNonce(toBigInt(reply.getChannelNonce()))
            .setSignedAmount(toBigInt(reply.getSignedAmount()));

        if (!reply.getSignature().isEmpty()) {
            builder.setSignature(new Signature(reply.getSignature().toByteArray()));
        }

        return builder.build();
    }

    //FIXME: reuse implementation from PaymentChannelStateService
    private static ByteString toBytesString(BigInteger value) {
        return ByteString.copyFrom(Utils.bigIntToBytes32(value));
    }

    //FIXME: reuse implementation from PaymentChannelStateService
    private static BigInteger toBigInt(ByteString value) {
        return Utils.bytes32ToBigInt(value.toByteArray());
    }

    static class MessageSigningHelper {

        private static final byte[] START_CLAIM_PREFIX = Utils.strToBytes("__start_claim");

        public static Signature signStartClaimRequest(BigInteger channelId, Address mpeAddress,
                BigInteger channelNonce, Identity signer) {
            return Utils.wrapExceptions(() -> {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bytes.write(START_CLAIM_PREFIX);
                bytes.write(mpeAddress.toByteArray());
                bytes.write(Utils.bigIntToBytes32(channelId));
                bytes.write(Utils.bigIntToBytes32(channelNonce));
                return signer.sign(bytes.toByteArray());
            });
        }

        private static final byte[] GET_LIST_INCLAIMED_PREFIX = Utils.strToBytes("__list_unclaimed");

        public static Signature signGetListUnclaimedRequest(Address mpeAddress,
                BigInteger currentBlock, Identity signer) {
            return Utils.wrapExceptions(() -> {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bytes.write(GET_LIST_INCLAIMED_PREFIX);
                bytes.write(mpeAddress.toByteArray());
                bytes.write(Utils.bigIntToBytes32(currentBlock));
                return signer.sign(bytes.toByteArray());
            });
        }
    }

}
