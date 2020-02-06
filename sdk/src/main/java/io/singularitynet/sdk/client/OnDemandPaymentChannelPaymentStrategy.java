package io.singularitynet.sdk.client;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.ToString;

import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.ethereum.WithAddress;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.registry.PaymentGroup;
import io.singularitynet.sdk.registry.PriceModel;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.mpe.PaymentChannelManager;

@ToString
public class OnDemandPaymentChannelPaymentStrategy extends EscrowPaymentStrategy {

    private final Ethereum ethereum;
    private final PaymentChannelManager channelManager;

    private final BigInteger expirationAdvance;
    private final BigInteger callsAdvance;
        
    public OnDemandPaymentChannelPaymentStrategy(Sdk sdk) {
        super(sdk);
        this.ethereum = sdk.getEthereum();
        this.channelManager = sdk.getPaymentChannelManager();
        this.expirationAdvance = BigInteger.valueOf(1);
        this.callsAdvance = BigInteger.valueOf(1);
    }

    @Override
    protected PaymentChannel selectChannel(ServiceClient serviceClient) {
        MetadataProvider metadataProvider = serviceClient.getMetadataProvider();

        String groupName = serviceClient.getEndpointGroupName();
        EndpointGroup endpointGroup = metadataProvider
            .getServiceMetadata()
            // TODO: what does guarantee that endpoint group name is not
            // changed before actual call is made? Think about it when
            // implementing failover strategy.
            .getEndpointGroupByName(groupName).get();

        BigInteger price = endpointGroup.getPricing().stream()
            .filter(pr -> pr.getPriceModel() == PriceModel.FIXED_PRICE)
            .findFirst().get()
            .getPriceInCogs();

        PaymentGroup paymentGroup = metadataProvider
            .getOrganizationMetadata()
            .getPaymentGroupById(endpointGroup.getPaymentGroupId()).get();
        BigInteger expirationThreshold = paymentGroup
            .getPaymentDetails()
            .getPaymentExpirationThreshold();
        BigInteger currentBlock = ethereum.getEthBlockNumber();
        BigInteger minExpiration = currentBlock.add(expirationThreshold);

        Optional<Supplier<PaymentChannel>> channelSupplier = channelManager
            .getChannelsAccessibleBy(paymentGroup.getPaymentGroupId(), getSigner())
            .map(ch -> ch.getChannelId())
            .map(id -> serviceClient.getPaymentChannelStateProvider()
                    .getChannelStateById(id))
            .flatMap(channel -> {
                if (channel.getBalance().compareTo(price) >= 0 && 
                    channel.getExpiration().compareTo(minExpiration) > 0) {
                    return Stream.of(() -> channel);
                }

                if (channel.getExpiration().compareTo(minExpiration) > 0) {
                    return Stream.of(() -> channelManager.addFundsToChannel(
                                channel, callsAdvance.multiply(price)));
                }

                if (channel.getBalance().compareTo(price) >= 0) {
                    return Stream.of(() -> channelManager.extendChannel(
                                channel, expirationThreshold.add(expirationAdvance)));
                }

                return Stream.<Supplier<PaymentChannel>>of(() -> channelManager.extendAndAddFundsToChannel(
                            channel, expirationThreshold.add(expirationAdvance),
                            callsAdvance.multiply(price)));
            })
            .findFirst();

        if (channelSupplier.isPresent()) {
            return channelSupplier.get().get();
        }

        return channelManager.openPaymentChannel(
                paymentGroup.getPaymentGroupId(),
                paymentGroup.getPaymentDetails().getPaymentAddress(),
                getSigner(),
                callsAdvance.multiply(price),
                expirationThreshold.add(expirationAdvance));
    }

}
