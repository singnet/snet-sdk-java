package io.singularitynet.sdk.daemon;

import com.google.protobuf.ByteString;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.daemon.escrow.StateService.*;
import io.singularitynet.daemon.escrow.FreeCallStateServiceGrpc;
import io.singularitynet.daemon.escrow.FreeCallStateServiceGrpc.*;

import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.PaymentGroupId;
import io.singularitynet.sdk.freecall.FreeCallPayment;

public class FreeCallStateService {

    private final static Logger log = LoggerFactory.getLogger(FreeCallStateService.class);

    private final String orgId;
    private final String serviceId;
    private final Ethereum ethereum;
    private final MetadataProvider metadataProvider;
    private final DaemonConnection daemonConnection;
    private final FreeCallStateServiceBlockingStub stub;

    public FreeCallStateService(String orgId, String serviceId, Ethereum ethereum,
            MetadataProvider metadataProvider, DaemonConnection daemonConnection) {
        this.orgId = orgId;
        this.serviceId = serviceId;
        this.ethereum = ethereum;
        this.metadataProvider = metadataProvider;
        this.daemonConnection = daemonConnection;
        this.stub = this.daemonConnection.getGrpcStub(FreeCallStateServiceGrpc::newBlockingStub);
    }

    public long getFreeCallsAvailable(String dappUserId, String freeCallToken,
            BigInteger tokenExpirationBlock, Identity signer) {
        log.info("Requesting number of free calls from daemon, dappUserId: {}, freeCallToken: {}, tokenExpirationBlock: {}, signer: {}",
                dappUserId, freeCallToken, tokenExpirationBlock, signer);

        String endpointGroupName = daemonConnection.getEndpointGroupName();
        EndpointGroup endpointGroup = metadataProvider
            .getServiceMetadata()
            // TODO: what does guarantee that endpoint group name is not
            // changed before actual call is made? Think about it when
            // implementing failover strategy.
            .getEndpointGroupByName(endpointGroupName).get();
        PaymentGroupId paymentGroupId = endpointGroup.getPaymentGroupId();
        BigInteger currentBlock = ethereum.getEthBlockNumber();

        FreeCallPayment payment = FreeCallPayment.newBuilder()
            .setSigner(signer)
            .setDappUserId(dappUserId)
            .setTokenExpirationBlock(tokenExpirationBlock)
            .setToken(freeCallToken)
            .setCurrentBlockNumber(currentBlock)
            .setOrgId(orgId)
            .setServiceId(serviceId)
            .setPaymentGroupId(paymentGroupId)
            .build();
        
        FreeCallStateRequest request = FreeCallStateRequest.newBuilder()
            .setUserId(dappUserId)
            .setTokenForFreeCall(ByteString.copyFrom(payment.getToken()))
            .setTokenExpiryDateBlock(tokenExpirationBlock.longValue())
            .setSignature(ByteString.copyFrom(payment.getSignature().getBytes()))
            .setCurrentBlock(currentBlock.longValue())
            .build();

        FreeCallStateReply reply = stub.getFreeCallsAvailable(request);
        
        long freeCallsAvailable = reply.getFreeCallsAvailable();
        log.info("Free calls available: {}", freeCallsAvailable);
        return freeCallsAvailable;
    }

}
