package io.singularitynet.sdk.paymentstrategy;

import java.math.BigInteger;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.payment.Payment;
import io.singularitynet.sdk.freecall.FreeCallPayment;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.GrpcCallParameters;

/**
 * Free call payment strategy implementation. Free call auth token emitted by
 * free call signer address guarantees that given the DApp user id owns signer
 * Ethereum address. Strategy uses signer private key and token to emit new
 * free call payments for the given service client.
 */
@ToString
public class FreeCallPaymentStrategy implements PaymentStrategy {

    private final static Logger log = LoggerFactory.getLogger(FreeCallPaymentStrategy.class);

    @ToString.Exclude
    private final Ethereum ethereum;

    private final String dappUserId;
    private final String freeCallToken;
    private final BigInteger tokenExpirationBlock;
    private final Identity signer;

    @ToString.Exclude
    private final FreeCallPayment.Builder paymentBuilder;

    /**
     * Constructor.
     * @param ethereum Ethereum client instance to get current ethereum block
     * @param signer identity which owns Ethereum address used to emit the
     * token; it is used to sign free call payment
     * @param dappUserId DApp user id which is used to calculate number of free
     * calls available
     * @param tokenExpirationBlock Ethereum block after which the token is
     * expired
     * @param freeCallToken token emitted by free call signer; in order to receive a
     * token one need to login to DApp using dappUserId and register an
     * Ethereum address of the signer there.
     */
    public FreeCallPaymentStrategy(Ethereum ethereum, Identity signer,
            String dappUserId, BigInteger tokenExpirationBlock, String freeCallToken) {
        this.ethereum = ethereum;
        this.dappUserId = dappUserId;
        this.tokenExpirationBlock = tokenExpirationBlock;
        this.freeCallToken = freeCallToken;
        this.signer = signer;
        this.paymentBuilder = FreeCallPayment.newBuilder()
            .setSigner(signer)
            .setDappUserId(dappUserId)
            .setTokenExpirationBlock(tokenExpirationBlock)
            .setToken(freeCallToken);
    }

    @Override
    public <ReqT, RespT> Payment getPayment(
            GrpcCallParameters<ReqT, RespT> parameters, ServiceClient serviceClient) {

        MetadataProvider metadataProvider = serviceClient.getMetadataProvider();
        String groupName = serviceClient.getEndpointGroupName();
        log.debug("Current endpoint group name: {}", groupName);

        EndpointGroup endpointGroup = metadataProvider
            .getServiceMetadata()
            // TODO: what does guarantee that endpoint group name is not
            // changed before actual call is made? Think about it when
            // implementing failover strategy.
            .getEndpointGroupByName(groupName).get();

        if (endpointGroup.getFreeCalls() == 0) {
            log.debug("Free calls are not configured for service id: {}, return invalid payment",
                    serviceClient.getServiceId());
            return Payment.INVALID_PAYMENT;
        }
        long freeCallAvailable = serviceClient.getFreeCallStateService()
            .getFreeCallsAvailable(dappUserId, freeCallToken,
                    tokenExpirationBlock, signer);
        if (freeCallAvailable <= 0) {
            log.debug("No free calls available for service id: {}, return invalid payment",
                    serviceClient.getServiceId());
            return Payment.INVALID_PAYMENT;
        }
        
        log.debug("Service id: {}, number of free calls available: {}, return free call payment",
                serviceClient.getServiceId(), freeCallAvailable);
        return paymentBuilder
            .setCurrentBlockNumber(ethereum.getEthBlockNumber())
            .setOrgId(serviceClient.getOrgId())
            .setServiceId(serviceClient.getServiceId())
            .setPaymentGroupId(endpointGroup.getPaymentGroupId())
            .build();
    }

}
