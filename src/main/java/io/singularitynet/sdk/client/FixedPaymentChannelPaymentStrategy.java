package io.singularitynet.sdk.client;

import java.math.BigInteger;
import java.util.Arrays;

import io.singularitynet.sdk.ethereum.Signer;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.mpe.*;
import io.singularitynet.sdk.registry.*;

public class FixedPaymentChannelPaymentStrategy implements PaymentStrategy {
        
    private final BigInteger channelId;
    private final Signer signer;

    public FixedPaymentChannelPaymentStrategy(BigInteger channelId, Signer signer) {
        this.channelId = channelId;
        this.signer = signer;
    }

    @Override
    public <ReqT, RespT> Payment getPayment(GrpcCallParameters<ReqT, RespT> callParams,
            ServiceClient serviceClient) {
        PaymentChannel channel = serviceClient.getPaymentChannelProvider()
            .getChannelById(channelId);
        BigInteger price = getPrice(channel, serviceClient);
        // TODO: test on price exceeds channel value
        BigInteger newAmount = channel.getSpentAmount().add(price);
        return EscrowPayment.newBuilder()
            .setPaymentChannel(channel)
            .setAmount(newAmount)
            .setSigner(signer)
            .build();
    }

    private BigInteger getPrice(PaymentChannel channel, ServiceClient serviceClient) {
        ServiceMetadata serviceMetadata = serviceClient.getMetadataProvider().getServiceMetadata();
        // TODO: this can contradict to failover strategy:
        // how to align endpoint group selected by failover and payment group?
        EndpointGroup group = serviceMetadata.getEndpointGroups().stream()
            .filter(grp -> Arrays.equals(channel.getPaymentGroupId(), grp.getPaymentGroupId()))
            .findFirst().get();
        Pricing price = group.getPricing().stream()
            .filter(pr -> PriceModel.FIXED_PRICE.equals(pr.getPriceModel()))
            .findFirst().get();
        return price.getPriceInCogs();
    }

}
