package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;
import io.singularitynet.sdk.ethereum.MnemonicIdentity;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.client.StaticConfiguration;

public class AbstractIntegrationTest {

    protected Sdk sdk;

    @Before
    public void setUp() {
        PrivateKeyIdentity caller = setupNewIdentity();

        StaticConfiguration config = IntEnv.newTestConfigurationBuilder()
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(caller.getCredentials().getEcKeyPair().getPrivateKey().toByteArray())
            .build();
        
        this.sdk = new Sdk(config);
    }

    @After
    public void tearDown() {
        sdk.close();
    }

    public void run(PaymentStrategy paymentStrategy, Consumer<ServiceClient> test) {
            ServiceClient serviceClient = sdk.newServiceClient(IntEnv.TEST_ORG_ID,
                    IntEnv.TEST_SERVICE_ID, IntEnv.TEST_ENDPOINT_GROUP, paymentStrategy); 
            try {
                
                test.accept(serviceClient);

            } finally {
                serviceClient.close();
            }
    }

    private static PrivateKeyIdentity setupNewIdentity() {
        PrivateKeyIdentity identity = new MnemonicIdentity("random mnemonic #" + Math.random(), 0);

        PrivateKeyIdentity deployer = new PrivateKeyIdentity(IntEnv.DEPLOYER_PRIVATE_KEY);
        Configuration config = IntEnv.newTestConfigurationBuilder()
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(deployer.getCredentials().getEcKeyPair().getPrivateKey().toByteArray())
            .build();
        Sdk sdk = new Sdk(config);
        Web3j web3j = Web3j.build(new HttpService(IntEnv.ETHEREUM_JSON_RPC_ENDPOINT));
        try {
            // share ETH
            Transfer.sendFunds(web3j, deployer.getCredentials(),
                    identity.getAddress().toString(),
                    BigDecimal.valueOf(1.0), Convert.Unit.ETHER).send();
            // share AGI
            sdk.transfer(identity.getAddress(), BigInteger.valueOf(1000000));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            web3j.shutdown();
            sdk.close();
        }

        return identity;
    }

}
