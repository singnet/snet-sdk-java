package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;

import java.math.BigInteger;

import io.singularitynet.sdk.ethereum.*;
import io.singularitynet.sdk.registry.*;
import io.singularitynet.sdk.daemon.*;
import io.singularitynet.sdk.client.*;
import io.singularitynet.sdk.paymentstrategy.FixedPaymentChannelPaymentStrategy;

public class CallAfterClaimTestIT {

    private Sdk sdk;
    private ServiceClient serviceClient;
    private BigInteger channelId;

    private DaemonConnection deployerConnection;
    private ProviderControlService controlService;

    @Before
    public void setUp() {
        Configuration config = IntEnv.newTestConfigurationBuilder()
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(IntEnv.CALLER_PRIVATE_KEY)
            .build();
        this.sdk = new Sdk(config);
        MetadataProvider metadataProvider = this.sdk.getMetadataProvider(
                IntEnv.TEST_ORG_ID, IntEnv.TEST_SERVICE_ID);
        PaymentGroupId paymentGroupId = metadataProvider.getServiceMetadata()
            .getEndpointGroupByName(IntEnv.TEST_ENDPOINT_GROUP).get()
            .getPaymentGroupId();
        PaymentGroup paymentGroup = metadataProvider.getOrganizationMetadata()
            .getPaymentGroupById(paymentGroupId).get();
        this.channelId = this.sdk.getBlockchainPaymentChannelManager()
            .openPaymentChannel(paymentGroup, this.sdk.getIdentity(),
                    BigInteger.valueOf(3), BigInteger.valueOf(1).add(
                        paymentGroup.getPaymentDetails()
                        .getPaymentExpirationThreshold()))
            .getChannelId();
        this.serviceClient = sdk.newServiceClient(IntEnv.TEST_ORG_ID,
                IntEnv.TEST_SERVICE_ID, IntEnv.TEST_ENDPOINT_GROUP,
                new FixedPaymentChannelPaymentStrategy(channelId));

        this.deployerConnection = new BaseDaemonConnection(
                new FixedGroupEndpointSelector(IntEnv.TEST_ENDPOINT_GROUP),
                sdk.getEthereum(),
                sdk.getMetadataProvider(IntEnv.TEST_ORG_ID, IntEnv.TEST_SERVICE_ID));
        PrivateKeyIdentity deployer = new PrivateKeyIdentity(IntEnv.DEPLOYER_PRIVATE_KEY);
        this.controlService = new ProviderControlService(deployerConnection,
                IntEnv.MPE_CONTRACT_ADDRESS, deployer, sdk.getEthereum());
    }

    @After
    public void tearDown() {
        deployerConnection.shutdownNow();
        serviceClient.close();
        sdk.close();
    }

    @Test
    public void callTwiceAfterClaim() {
        IntEnv.makeServiceCall(serviceClient);

        PaymentReply reply = controlService.startClaim(channelId); 
        assertEquals("Channel id in claim reply", channelId, reply.getChannelId());

        IntEnv.makeServiceCall(serviceClient);
        IntEnv.makeServiceCall(serviceClient);
    }

}
