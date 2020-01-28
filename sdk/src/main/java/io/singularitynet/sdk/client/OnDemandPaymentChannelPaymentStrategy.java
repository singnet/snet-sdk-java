package io.singularitynet.sdk.client;

import java.math.BigInteger;
import lombok.ToString;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.registry.*;

@ToString
public class OnDemandPaymentChannelPaymentStrategy extends PaymentChannelPaymentStrategy {

    private final BigInteger expirationAdvance;
    private final BigInteger callsAdvance;
        
    public OnDemandPaymentChannelPaymentStrategy() {
        this.expirationAdvance = BigInteger.valueOf(2);
        this.callsAdvance = BigInteger.valueOf(1);
    }

    @Override
    protected PaymentChannel selectChannel(ServiceClient serviceClient) {
        MetadataProvider metadataProvider = serviceClient.getMetadataProvider();
        OrganizationMetadata orgMetadata = metadataProvider.getOrganizationMetadata();
        ServiceMetadata serviceMetadata = metadataProvider.getServiceMetadata();

        String groupName = serviceClient.getDaemonConnection().getEndpointGroupName();
        EndpointGroup endpointGroup = serviceMetadata.getEndpointGroups()
            .stream()
            .filter(eg -> eg.getGroupName().equals(groupName))
            .findFirst()
            .get();
        PaymentGroup paymentGroup = orgMetadata.getPaymentGroups()
            .stream()
            .filter(pg -> pg.getPaymentGroupId().equals(endpointGroup.getPaymentGroupId()))
            .findFirst()
            .get();

        Address signer = serviceClient.getSigner().getAddress();
        Address recipient = paymentGroup.getPaymentDetails().getPaymentAddress();
        PaymentGroupId groupId = paymentGroup.getPaymentGroupId();
        BigInteger expirationThreshold = paymentGroup.getPaymentDetails().getPaymentExpirationThreshold();
        BigInteger lifetimeInBlocks = expirationThreshold.add(expirationAdvance);

        Pricing pricing = endpointGroup.getPricing()
            .stream()
            .filter(price -> PriceModel.FIXED_PRICE.equals(price.getPriceModel()))
            .findFirst()
            .get();
        BigInteger value = pricing.getPriceInCogs().multiply(callsAdvance);

        PaymentChannel channel = serviceClient.getPaymentChannelProvider()
            .openChannel(signer, recipient, groupId, value, lifetimeInBlocks);

        return channel;
    }

}
