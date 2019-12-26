package io.singularitynet.sdk.client;

import java.math.BigInteger;
import java.util.Arrays;
import lombok.ToString;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.daemon.Payment;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.mpe.EscrowPayment;
import io.singularitynet.sdk.registry.*;

/**
 * The class is responsible for providing a payment for the client call using
 * the specified payment channel.
 */
@ToString
public class FixedPaymentChannelPaymentStrategy implements PaymentStrategy {
        
    private final BigInteger channelId;

    /**
     * Constructor.
     * @param channelId id of the payment channel to use for the payment
     * generation.
     */
    public FixedPaymentChannelPaymentStrategy(BigInteger channelId) {
        this.channelId = channelId;
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
            .setSigner(serviceClient.getSigner())
            .build();
    }

    private BigInteger getPrice(PaymentChannel channel, ServiceClient serviceClient) {
        ServiceMetadata serviceMetadata = serviceClient.getMetadataProvider().getServiceMetadata();
        // TODO: this can contradict to failover strategy:
        // how to align endpoint group selected by failover and payment group?
        EndpointGroup group = serviceMetadata.getEndpointGroups().stream()
            .filter(grp -> channel.getPaymentGroupId().equals(grp.getPaymentGroupId()))
            .findFirst().get();
        Pricing price = group.getPricing().stream()
            .filter(pr -> PriceModel.FIXED_PRICE.equals(pr.getPriceModel()))
            .findFirst().get();
        BigInteger priceInCogs = price.getPriceInCogs();
        return priceInCogs;
    }

}
