package io.singularitynet.sdk.client;

import java.math.BigInteger;

import lombok.ToString;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.payment.Payment;
import io.singularitynet.sdk.freecall.FreeCallPayment;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.EndpointGroup;

/**
 * Free call payment strategy implementation. Free call auth token emitted by
 * free call signer address guarantees that given the DApp user id owns signer
 * Ethereum address. Strategy uses signer private key and token to emit new
 * free call payments for the given service client.
 */
@ToString
public class FreeCallPaymentStrategy implements PaymentStrategy {

    @ToString.Exclude
    private final Ethereum ethereum;
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
     * @param token token emitted by free call signer; in order to receive a
     * token one need to login to DApp using dappUserId and register an
     * Ethereum address of the signer there.
     * @return new free call payment strategy instance
     */
    public FreeCallPaymentStrategy(Ethereum ethereum, Identity signer,
            String dappUserId, BigInteger tokenExpirationBlock, String token) {
        this.ethereum = ethereum;
        this.paymentBuilder = FreeCallPayment.newBuilder()
            .setSigner(signer)
            .setDappUserId(dappUserId)
            .setTokenExpirationBlock(tokenExpirationBlock)
            .setToken(token);
    }

    @Override
    public <ReqT, RespT> Payment getPayment(
            GrpcCallParameters<ReqT, RespT> parameters, ServiceClient serviceClient) {

        MetadataProvider metadataProvider = serviceClient.getMetadataProvider();
        String groupName = serviceClient.getEndpointGroupName();

        EndpointGroup endpointGroup = metadataProvider
            .getServiceMetadata()
            // TODO: what does guarantee that endpoint group name is not
            // changed before actual call is made? Think about it when
            // implementing failover strategy.
            .getEndpointGroupByName(groupName).get();
        
        return paymentBuilder
            .setCurrentBlockNumber(ethereum.getEthBlockNumber())
            .setOrgId(serviceClient.getOrgId())
            .setServiceId(serviceClient.getServiceId())
            .setPaymentGroupId(endpointGroup.getPaymentGroupId())
            .build();
    }

}
