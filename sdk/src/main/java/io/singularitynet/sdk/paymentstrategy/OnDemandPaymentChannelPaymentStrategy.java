package io.singularitynet.sdk.paymentstrategy;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.registry.PaymentGroup;
import io.singularitynet.sdk.registry.PriceModel;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.mpe.BlockchainPaymentChannelManager;
import io.singularitynet.sdk.client.ServiceClient;

/**
 * Payment channel strategy which manages channel on demand. It tries to find
 * ready to use channel if found that it is returned. If there are any expired
 * or zero balanced channels then strategy updates them. If there are no
 * payment channel to use it opens new one. In order to update and open
 * channels this strategy requires identity which has non zero Ethereum
 * balance. Moreover this identity should have enough number of AGI tokens
 * deposited in MultiPartyEscrow contract. Strategy uses fixed price model to
 * calculate amount of tokens required.
 */
@ToString
public class OnDemandPaymentChannelPaymentStrategy extends EscrowPaymentStrategy {

    private final static Logger log = LoggerFactory.getLogger(OnDemandPaymentChannelPaymentStrategy.class);

    private final BigInteger channelLifetime;
    private final BigInteger numberOfCalls;
        
    /**
     * New on demand payment channel strategy with default parameter values.
     */
    public OnDemandPaymentChannelPaymentStrategy() {
        this(1, 1);
    }

    /**
     * New on demand payment channel strategy.
     * @param channelLifetime number of blocks to be added to the service
     * provider expiration threshold when opening or updating channels.
     * @see io.singularitynet.sdk.registry.PaymentDetails#getPaymentExpirationThreshold
     * @param numberOfCalls number of calls by fixed price to be made after
     * channel is opened or updated.
     */
    public OnDemandPaymentChannelPaymentStrategy(long channelLifetime,
            long numberOfCalls) {
        this(BigInteger.valueOf(channelLifetime), BigInteger.valueOf(numberOfCalls));
    }

    /**
     * New on demand payment channel strategy.
     * @param channelLifetime number of blocks to be added to the service
     * provider expiration threshold when opening or updating channels.
     * @see io.singularitynet.sdk.registry.PaymentDetails#getPaymentExpirationThreshold
     * @param numberOfCalls number of calls by fixed price to be made after
     * channel is opened or updated.
     */
    public OnDemandPaymentChannelPaymentStrategy(BigInteger channelLifetime,
            BigInteger numberOfCalls) {
        this.channelLifetime = channelLifetime;
        this.numberOfCalls = numberOfCalls;
    }

    @Override
    protected PaymentChannel selectChannel(ServiceClient serviceClient) {
        log.debug("Selecting channel to make a call using service client");
        MetadataProvider metadataProvider = serviceClient.getMetadataProvider();
        BlockchainPaymentChannelManager blockchainChannelManager = serviceClient.getSdk().getBlockchainPaymentChannelManager();
        Identity signer = serviceClient.getSdk().getIdentity();

        EndpointGroup endpointGroup = getEndpointGroup(serviceClient);

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
        BigInteger currentBlock = serviceClient.getSdk().getEthereum().getEthBlockNumber();
        BigInteger minExpiration = currentBlock.add(expirationThreshold);

        Optional<Supplier<PaymentChannel>> channelSupplier = blockchainChannelManager
            .getChannelsAccessibleBy(paymentGroup.getPaymentGroupId(), signer)
            .map(ch -> ch.getChannelId())
            .map(id -> serviceClient.getPaymentChannelStateProvider()
                    .getChannelStateById(id, signer))
            .flatMap(channel -> {
                if (channel.getBalance().compareTo(price) >= 0 && 
                    channel.getExpiration().compareTo(minExpiration) > 0) {
                    log.debug("Channel found: {}", channel);
                    return Stream.of(() -> channel);
                }

                if (channel.getExpiration().compareTo(minExpiration) > 0) {
                    BigInteger amount = numberOfCalls.multiply(price);
                    log.info("Channel found: {}, adding funds: {} cogs", channel, amount);
                    return Stream.of(() -> blockchainChannelManager.addFundsToChannel(
                                channel, amount));
                }

                if (channel.getBalance().compareTo(price) >= 0) {
                    BigInteger lifetime = expirationThreshold.add(channelLifetime);
                    log.info("Channel found: {}, extending expiration date on: {} blocks", channel, lifetime);
                    return Stream.of(() -> blockchainChannelManager.extendChannel(
                                channel, lifetime));
                }

                BigInteger amount = numberOfCalls.multiply(price);
                BigInteger lifetime = expirationThreshold.add(channelLifetime);
                log.info("Channel found: {}, adding funds: {} cogs, and extending lifetime: {}",
                        channel, amount, lifetime);
                return Stream.<Supplier<PaymentChannel>>of(() -> blockchainChannelManager
                        .extendAndAddFundsToChannel(channel, lifetime, amount));
            })
            .findFirst();

        if (channelSupplier.isPresent()) {
            return channelSupplier.get().get();
        }

        PaymentChannel channel = blockchainChannelManager.openPaymentChannel(
                paymentGroup,
                signer,
                numberOfCalls.multiply(price),
                expirationThreshold.add(channelLifetime));
        log.info("New channel opened: {}", channel);
        return channel;
    }

}
