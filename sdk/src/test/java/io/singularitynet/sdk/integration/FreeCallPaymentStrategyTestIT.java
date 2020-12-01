package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Random;

import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.freecall.FreeCallPayment;
import io.singularitynet.sdk.freecall.FreeCallAuthToken;
import io.singularitynet.sdk.paymentstrategy.FreeCallPaymentStrategy;
import io.singularitynet.sdk.client.Sdk;

public class FreeCallPaymentStrategyTestIT extends AbstractIntegrationTest {

    @Test
    public void useFreeCallPayment() {
        FreeCallPaymentStrategy strategy = getFreeCallPaymentStrategy(sdk);
        run(strategy, serviceClient -> {

               IntEnv.makeServiceCall(serviceClient); 

        });
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void freeCallRejectedWhenNotConfigured() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("No payment returned by PaymentStrategy");

        FreeCallPaymentStrategy strategy = getFreeCallPaymentStrategy(sdk);
        run(strategy, serviceClient -> {

            boolean twoCalls = false;
            try {

               IntEnv.makeServiceCall(serviceClient); 
               IntEnv.makeServiceCall(serviceClient); 
               twoCalls = true;
               IntEnv.makeServiceCall(serviceClient); 

            } finally {
                assertTrue("Two calls made successfully", twoCalls);
            }

        });
    }

    public static FreeCallPaymentStrategy getFreeCallPaymentStrategy(Sdk sdk) {
        String dappUserId = randomDappUserId();
        Identity caller = sdk.getIdentity();
        BigInteger freeCallExpirationBlock = BigInteger.valueOf(1)
            .add(sdk.getEthereum().getEthBlockNumber());
        String freeCallToken = FreeCallPayment.generateFreeCallPaymentToken(
                dappUserId, caller.getAddress(), freeCallExpirationBlock,
                IntEnv.DEPLOYER_IDENTITY);
        return new FreeCallPaymentStrategy(caller,
                FreeCallAuthToken.newBuilder()
                    .setDappUserId(dappUserId)
                    .setExpirationBlock(freeCallExpirationBlock)
                    .setToken(freeCallToken)
                    .build());
    }

    private static final Random random = new Random();

    private static String randomDappUserId() {
        return "user-" + random.nextInt() + "@mail.com";
    }

}
