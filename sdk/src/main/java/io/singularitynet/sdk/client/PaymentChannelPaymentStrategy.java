package io.singularitynet.sdk.client;

import java.math.BigInteger;

import io.singularitynet.sdk.daemon.Payment;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.mpe.EscrowPayment;
import io.singularitynet.sdk.registry.*;

// TODO: replace inheritance of FixedPaymentChannelPaymentStrategy and
// OnDemandPaymentChannelPaymentStrategy from PaymentChannelPaymentStrategy by
// aggregation
public abstract class PaymentChannelPaymentStrategy implements PaymentStrategy {
        
    protected abstract PaymentChannel selectChannel(ServiceClient serviceClient);

    @Override
    public <ReqT, RespT> Payment getPayment(GrpcCallParameters<ReqT, RespT> callParams,
            ServiceClient serviceClient) {
        PaymentChannel channel = selectChannel(serviceClient);
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
