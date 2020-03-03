package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;
import io.singularitynet.sdk.ethereum.MnemonicIdentity;
import io.singularitynet.sdk.ethereum.Signature;
import io.singularitynet.sdk.freecall.FreeCallPayment;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.FreeCallPaymentStrategy;
import io.singularitynet.sdk.client.CombinedPaymentStrategy;
import io.singularitynet.sdk.client.OnDemandPaymentChannelPaymentStrategy;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.ServiceClient;

public class CombinedPaymentStrategyTestIT {

    private Web3j web3j;

    @Before
    public void setUp() {
        web3j = Web3j.build(new HttpService(IntEnv.ETHEREUM_JSON_RPC_ENDPOINT));
    }

    @After
    public void tearDown() {
        web3j.shutdown();
    }

    // FIXME: remove code duplication with FreeCallPaymentStrategyTestIT and
    // OnDemandPaymentChannelPaymentStrategyTestIT
    @Test
    public void useChannelWhenFreeCallsAreOver() throws Exception {
        final String dappUserId = randomDappUserId();
        final PrivateKeyIdentity caller = setupNewIdentity();
        final BigInteger freeCallExpirationBlock = BigInteger.valueOf(1)
            .add(getLastEthereumBlock());
        final String freeCallToken = FreeCallPayment.generateFreeCallPaymentToken(
                dappUserId, caller.getAddress(), freeCallExpirationBlock,
                IntEnv.DEPLOYER_IDENTITY);

        Configuration config = IntEnv.newTestConfigurationBuilder()
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(caller.getCredentials().getEcKeyPair().getPrivateKey().toByteArray())
            .build();
        Sdk sdk = new Sdk(config);
        try {

            Identity freeCallSigner = sdk.getIdentity();
            CombinedPaymentStrategy strategy = new CombinedPaymentStrategy(
                    new FreeCallPaymentStrategy(sdk.getEthereum(),
                        freeCallSigner, dappUserId, freeCallExpirationBlock,
                        freeCallToken),
                    new OnDemandPaymentChannelPaymentStrategy(sdk));
            ServiceClient serviceClient = sdk.newServiceClient(
                    IntEnv.TEST_ORG_ID, IntEnv.TEST_SERVICE_ID,
                    IntEnv.TEST_ENDPOINT_GROUP, strategy);
            try {

               IntEnv.makeServiceCall(serviceClient); 
               IntEnv.makeServiceCall(serviceClient); 
               IntEnv.makeServiceCall(serviceClient); 

            } finally {
                serviceClient.shutdownNow();
            }

        } finally {
            sdk.shutdown();
        }
    }

    private BigInteger getLastEthereumBlock() {
        return Utils.wrapExceptions(() -> web3j.ethBlockNumber().send()
                .getBlockNumber());
    }

    private static final Random random = new Random();

    private static String randomDappUserId() {
        return "user-" + random.nextInt() + "@mail.com";
    }

    private PrivateKeyIdentity setupNewIdentity() throws Exception {
        PrivateKeyIdentity identity = new MnemonicIdentity("random mnemonic #" + Math.random(), 0);

        Web3j web3j = Web3j.build(new HttpService(IntEnv.ETHEREUM_JSON_RPC_ENDPOINT));
        try {
            PrivateKeyIdentity deployer = new PrivateKeyIdentity(IntEnv.DEPLOYER_PRIVATE_KEY);
            Transfer.sendFunds(web3j, deployer.getCredentials(), identity.getAddress().toString(),
                    BigDecimal.valueOf(1.0), Convert.Unit.ETHER).send();
        } finally {
            web3j.shutdown();
        }

        Configuration config = IntEnv.newTestConfigurationBuilder()
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(IntEnv.DEPLOYER_PRIVATE_KEY)
            .build();
        Sdk sdk = new Sdk(config);
        try {
            sdk.transfer(identity.getAddress(), BigInteger.valueOf(1000000));
        } finally {
            sdk.shutdown();
        }

        return identity;
    }

}
