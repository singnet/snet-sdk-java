package io.singularitynet.sdk.freecall;

import com.google.protobuf.ByteString;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.daemon.escrow.StateService.*;
import io.singularitynet.daemon.escrow.FreeCallStateServiceGrpc;
import io.singularitynet.daemon.escrow.FreeCallStateServiceGrpc.*;

import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.registry.PaymentGroupId;
import io.singularitynet.sdk.daemon.DaemonConnection;

public class FreeCallStateService {

    private final static Logger log = LoggerFactory.getLogger(FreeCallStateService.class);

    // TODO: get orgId and serviceId from MetadataProvider
    private final String orgId;
    private final String serviceId;
    private final DaemonConnection daemonConnection;
    private final Ethereum ethereum;
    private final FreeCallStateServiceBlockingStub stub;

    public FreeCallStateService(String orgId, String serviceId,
            DaemonConnection daemonConnection, Ethereum ethereum) {
        this.orgId = orgId;
        this.serviceId = serviceId;
        this.daemonConnection = daemonConnection;
        this.ethereum = ethereum;
        this.stub = this.daemonConnection.getGrpcStub(FreeCallStateServiceGrpc::newBlockingStub);
    }

    public long getFreeCallsAvailable(FreeCallAuthToken token, Identity signer) {
        log.info("Requesting number of free calls from daemon, token: {}, signer: {}",
                token, signer);

        EndpointGroup endpointGroup = daemonConnection.getEndpoint().getGroup();
        PaymentGroupId paymentGroupId = endpointGroup.getPaymentGroupId();
        BigInteger currentBlock = ethereum.getEthBlockNumber();

        FreeCallPayment payment = FreeCallPayment.newBuilder()
            .setSigner(signer)
            .setToken(token)
            .setCurrentBlockNumber(currentBlock)
            .setOrgId(orgId)
            .setServiceId(serviceId)
            .setPaymentGroupId(paymentGroupId)
            .build();
        
        FreeCallStateRequest request = FreeCallStateRequest.newBuilder()
            .setUserId(token.getDappUserId())
            .setTokenForFreeCall(ByteString.copyFrom(payment.getToken()))
            .setTokenExpiryDateBlock(token.getExpirationBlock().longValue())
            .setSignature(ByteString.copyFrom(payment.getSignature().getBytes()))
            .setCurrentBlock(currentBlock.longValue())
            .build();

        FreeCallStateReply reply = stub.getFreeCallsAvailable(request);
        
        long freeCallsAvailable = reply.getFreeCallsAvailable();
        log.info("Free calls available: {}", freeCallsAvailable);
        return freeCallsAvailable;
    }

}
