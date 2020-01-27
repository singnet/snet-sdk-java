package io.singularitynet.sdk.client;

import java.math.BigInteger;
import lombok.ToString;
import org.web3j.protocol.core.Ethereum;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import io.singularitynet.sdk.contracts.MultiPartyEscrow;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.registry.*;

// FIXME: rename to OnDemandPaymentChannelPaymentStrategy
@ToString
public class UpdatePaymentChannelPaymentStrategy extends PaymentChannelPaymentStrategy {

    private final MultiPartyEscrow mpe;
    private final Ethereum ethereum;
    private final BigInteger expirationAdvance;
    private final BigInteger callsAdvance;
        
    public UpdatePaymentChannelPaymentStrategy(Sdk sdk) {
        this.mpe = sdk.mpe; 
        this.ethereum = sdk.web3j;
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
        BigInteger currentBlock = Utils.wrapExceptions(() -> ethereum.ethBlockNumber().send().getBlockNumber());
        BigInteger expiration = currentBlock.add(expirationThreshold.add(expirationAdvance));

        Pricing pricing = endpointGroup.getPricing()
            .stream()
            .filter(price -> PriceModel.FIXED_PRICE.equals(price.getPriceModel()))
            .findFirst()
            .get();
        BigInteger value = pricing.getPriceInCogs().multiply(callsAdvance);

        TransactionReceipt transaction = Utils.wrapExceptions(() ->
                mpe.openChannel(signer.toString(), recipient.toString(),
                    groupId.getBytes(), value, expiration).send());
        MultiPartyEscrow.ChannelOpenEventResponse event = mpe.getChannelOpenEvents(transaction).get(0);

        PaymentChannel channel = PaymentChannel.fromChannelOpenEvent(event);

        return channel;
    }

}
