package io.singularitynet.sdk.client;

import java.math.BigInteger;

import io.singularitynet.sdk.payment.Payment;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.mpe.EscrowPayment;
import io.singularitynet.sdk.registry.*;

// TODO: replace inheritance of FixedPaymentChannelPaymentStrategy and
// OnDemandPaymentChannelPaymentStrategy from EscrowPaymentStrategy by
// aggregation
public abstract class EscrowPaymentStrategy implements PaymentStrategy {

    private final Identity signer;

    public EscrowPaymentStrategy(Sdk sdk) {
        this.signer = sdk.getIdentity();
    }

    protected Identity getSigner() {
        return signer;
    }

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
            .setSigner(signer)
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
