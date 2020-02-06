package io.singularitynet.sdk.client;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.ToString;
import org.web3j.protocol.Web3j;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.WithAddress;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.registry.PriceModel;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.mpe.PaymentChannelManager;

@ToString
public class OnDemandPaymentChannelPaymentStrategy extends EscrowPaymentStrategy {

    private final Web3j web3j;
    private final BigInteger expirationAdvance;
    private final BigInteger callsAdvance;
        
    public OnDemandPaymentChannelPaymentStrategy(Sdk sdk) {
        this.web3j = sdk.getWeb3j();
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

        BigInteger expirationThreshold = metadataProvider
            .getOrganizationMetadata()
            .getPaymentGroupById(endpointGroup.getPaymentGroupId()).get()
            .getPaymentDetails()
            .getPaymentExpirationThreshold();
        BigInteger currentBlock = Utils.wrapExceptions(() -> web3j.ethBlockNumber().send().getBlockNumber());
        BigInteger minExpiration = currentBlock.add(expirationThreshold);

        PaymentChannelManager channelManager = serviceClient.getPaymentChannelManager();
        WithAddress signer = serviceClient.getSigner();

        Optional<Supplier<PaymentChannel>> channelSupplier = channelManager
            .getChannelsAccessibleBy(endpointGroup.getPaymentGroupId(), signer)
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
                endpointGroup.getPaymentGroupId(),
                serviceClient.getSigner(),
                callsAdvance.multiply(price),
                expirationThreshold.add(expirationAdvance));
    }

}
