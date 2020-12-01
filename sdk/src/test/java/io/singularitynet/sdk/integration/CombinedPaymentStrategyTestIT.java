package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;

import io.singularitynet.sdk.paymentstrategy.CombinedPaymentStrategy;
import io.singularitynet.sdk.paymentstrategy.OnDemandPaymentChannelPaymentStrategy;

public class CombinedPaymentStrategyTestIT extends AbstractIntegrationTest {

    @Test
    public void useChannelWhenFreeCallsAreOver() {
        CombinedPaymentStrategy strategy = new CombinedPaymentStrategy(
                FreeCallPaymentStrategyTestIT.getFreeCallPaymentStrategy(sdk),
                new OnDemandPaymentChannelPaymentStrategy());

        run(strategy, serviceClient -> {

               IntEnv.makeServiceCall(serviceClient); 
               IntEnv.makeServiceCall(serviceClient); 
               IntEnv.makeServiceCall(serviceClient); 

        });
    }

}
