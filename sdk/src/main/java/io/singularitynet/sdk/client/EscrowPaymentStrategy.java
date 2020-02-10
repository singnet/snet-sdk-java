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
/**
 * Escrow payment strategy class which is based on MultiPartyEscrow contract
 * payment channel selection.
 */
public abstract class EscrowPaymentStrategy implements PaymentStrategy {

    private final Identity signer;

    /**
     * Constructor.
     * @param sdk SDK instance.
     */
    public EscrowPaymentStrategy(Sdk sdk) {
        this.signer = sdk.getIdentity();
    }

    /**
     * @return payment check signer identity.
     */
    protected Identity getSigner() {
        return signer;
    }

    /**
     * Select and return actual payment channel state which should be used for
     * payment.
     * @param serviceClient service client instance.
     * @return payment channel state.
     */
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
