package io.singularitynet.sdk.paymentstrategy;

import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.payment.Payment;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.mpe.EscrowPayment;
import io.singularitynet.sdk.registry.*;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.client.GrpcCallParameters;

/**
 * Escrow payment strategy class which is based on MultiPartyEscrow contract
 * payment channel selection.
 */
public abstract class EscrowPaymentStrategy implements PaymentStrategy {

    private final static Logger log = LoggerFactory.getLogger(EscrowPaymentStrategy.class);

    /**
     * Constructor.
     */
    public EscrowPaymentStrategy() {
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
            .setSigner(serviceClient.getSdk().getIdentity())
            .build();
    }

    protected EndpointGroup getEndpointGroup(ServiceClient serviceClient) {
        String groupName = serviceClient.getEndpointGroupName();
        log.debug("Current endpoint group name: {}", groupName);
        return serviceClient.getMetadataProvider()
            .getServiceMetadata()
            // TODO: what does guarantee that endpoint group name is not
            // changed before actual call is made? Think about it when
            // implementing failover strategy.
            .getEndpointGroupByName(groupName).get();
    }

    private BigInteger getPrice(PaymentChannel channel, ServiceClient serviceClient) {
        EndpointGroup group = getEndpointGroup(serviceClient);
        Pricing price = group.getPricing().stream()
            .filter(pr -> PriceModel.FIXED_PRICE.equals(pr.getPriceModel()))
            .findFirst().get();
        BigInteger priceInCogs = price.getPriceInCogs();
        return priceInCogs;
    }

}
