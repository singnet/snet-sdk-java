package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.singularitynet.sdk.ethereum.WithAddress;
import io.singularitynet.sdk.registry.PriceModel;
import io.singularitynet.sdk.registry.Pricing;
import io.singularitynet.sdk.registry.PaymentGroup;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.paymentstrategy.OnDemandPaymentChannelPaymentStrategy;

public class OnDemandPaymentChannelPaymentStrategyTestIT extends AbstractIntegrationTest {

    private PaymentGroup paymentGroup;
    private BigInteger cogsPerCall;
    private BigInteger expirationThreshold;

    @Before
    public void setUp() {
        super.setUp();

        MetadataProvider metadataProvider = sdk.getMetadataProvider(
                IntEnv.TEST_ORG_ID, IntEnv.TEST_SERVICE_ID);
        EndpointGroup endpointGroup = metadataProvider
            .getServiceMetadata()
            .getEndpointGroupByName(IntEnv.TEST_ENDPOINT_GROUP).get();
        this.paymentGroup = metadataProvider
            .getOrganizationMetadata()
            .getPaymentGroupById(endpointGroup.getPaymentGroupId()).get();
        Pricing servicePrice = endpointGroup 
            .getPricing().stream()
            .filter(pr -> pr.getPriceModel() == PriceModel.FIXED_PRICE)
            .findFirst().get();
        this.cogsPerCall = servicePrice.getPriceInCogs();
        this.expirationThreshold = paymentGroup.getPaymentDetails()
            .getPaymentExpirationThreshold();
    }

    @Test
    public void newChannelIsCreatedOnFirstCall() {
        run(new OnDemandPaymentChannelPaymentStrategy(), serviceClient -> {
            WithAddress caller = sdk.getIdentity();

            IntEnv.makeServiceCall(serviceClient);

            Stream<PaymentChannel> channels = getChannels(sdk, caller);
            assertEquals("Number of payment channels", 1, channels.count());
        });
    }

    @Test
    public void oldChannelIsReusedOnSecondCall() {
        run(new OnDemandPaymentChannelPaymentStrategy(), serviceClient -> {
            WithAddress caller = sdk.getIdentity();
            sdk.getBlockchainPaymentChannelManager().
                openPaymentChannel(paymentGroup, caller, cogsPerCall,
                    expirationThreshold.add(BigInteger.valueOf(1)));

            IntEnv.makeServiceCall(serviceClient);

            Stream<PaymentChannel> channels = getChannels(sdk, caller);
            assertEquals("Number of payment channels", 1, channels.count());
        });
    }

    @Test
    public void oldChannelAddFundsOnCall() {
        run(new OnDemandPaymentChannelPaymentStrategy(), serviceClient -> {
            WithAddress caller = sdk.getIdentity();
            sdk.getBlockchainPaymentChannelManager().
                openPaymentChannel(paymentGroup, caller, BigInteger.ZERO,
                    expirationThreshold.add(BigInteger.valueOf(2)));

            IntEnv.makeServiceCall(serviceClient);

            List<PaymentChannel> channels = getChannels(sdk, caller)
                .collect(Collectors.toList());
            assertEquals("Number of payment channels", 1, channels.size());
            assertEquals("Payment channel balance",
                    cogsPerCall, channels.get(0).getValue());
        });
    }

    @Test
    public void oldChannelIsExtendedOnCall() {
        run(new OnDemandPaymentChannelPaymentStrategy(), serviceClient -> {
            WithAddress caller = sdk.getIdentity();
            sdk.getBlockchainPaymentChannelManager().
                openPaymentChannel(paymentGroup, caller, cogsPerCall, BigInteger.ZERO);
            BigInteger blockBeforeCall = sdk.getEthereum().getEthBlockNumber();

            IntEnv.makeServiceCall(serviceClient);

            List<PaymentChannel> channels = getChannels(sdk, caller)
                .collect(Collectors.toList());
            assertEquals("Number of payment channels", 1, channels.size());
            assertEquals("Payment channel expiration block",
                    blockBeforeCall.add(expirationThreshold.add(BigInteger.valueOf(2))),
                    channels.get(0).getExpiration());
        });
    }

    @Test
    public void oldChannelIsExtendedAndFundsAddedOnCall() {
        run(new OnDemandPaymentChannelPaymentStrategy(), serviceClient -> {
            WithAddress caller = sdk.getIdentity();
            sdk.getBlockchainPaymentChannelManager().
                openPaymentChannel(paymentGroup, caller, BigInteger.ZERO, BigInteger.ZERO);
            BigInteger blockBeforeCall = sdk.getEthereum().getEthBlockNumber();

            IntEnv.makeServiceCall(serviceClient);

            List<PaymentChannel> channels = getChannels(sdk, caller)
                .collect(Collectors.toList());
            assertEquals("Number of payment channels", 1, channels.size());
            assertEquals("Payment channel expiration block",
                    blockBeforeCall.add(expirationThreshold.add(BigInteger.valueOf(2))),
                    channels.get(0).getExpiration());
            assertEquals("Payment channel balance",
                    cogsPerCall, channels.get(0).getValue());
        });
    }

    private Stream<PaymentChannel> getChannels(Sdk sdk, WithAddress caller) {
        return sdk.getBlockchainPaymentChannelManager()
            .getChannelsAccessibleBy(paymentGroup.getPaymentGroupId(), caller);
    }

}
