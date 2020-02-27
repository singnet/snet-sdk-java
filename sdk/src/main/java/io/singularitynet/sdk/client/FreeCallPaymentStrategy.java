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

// FIXME: add javadoc
@ToString
public class FreeCallPaymentStrategy implements PaymentStrategy {

    @ToString.Exclude
    private final Ethereum ethereum;
    private final FreeCallPayment.Builder paymentBuilder;

    public FreeCallPaymentStrategy(Ethereum ethereum, Identity signer,
            String dappUserId, BigInteger tokenExpirationBlock, String token) {
        this.ethereum = ethereum;
        this.paymentBuilder = FreeCallPayment.newBuilder()
            .setSigner(signer)
            .setDappUserId(dappUserId)
            .setTokenExpirationBlock(tokenExpirationBlock)
            .setToken(Utils.hexToBytes(token));
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
