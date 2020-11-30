package io.singularitynet.sdk.paymentstrategy;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;

import io.singularitynet.sdk.payment.Payment;
import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.ServiceMetadata;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.freecall.FreeCallAuthToken;
import io.singularitynet.sdk.client.ServiceClient;

public class FreeCallPaymentStrategyTest {

    @Test
    public void getPaymentNoFreeCallsConfiguration() {
        MetadataProvider metadataProvider = mock(MetadataProvider.class);
        when(metadataProvider.getServiceMetadata())
            .thenReturn(ServiceMetadata.newBuilder()
                    .addEndpointGroup(EndpointGroup.newBuilder()
                        .setGroupName("default_group")
                        .build())
                    .build());
        ServiceClient serviceClient = mock(ServiceClient.class);
        when(serviceClient.getMetadataProvider()).thenReturn(metadataProvider);
        when(serviceClient.getEndpointGroupName()).thenReturn("default_group");
        FreeCallAuthToken token = FreeCallAuthToken.newBuilder()
            .setDappUserId("user@email.com")
            .setExpirationBlock(BigInteger.ZERO)
            .setToken("010203")
            .build();
        FreeCallPaymentStrategy strategy = new FreeCallPaymentStrategy(
                mock(Identity.class), token);
        
        Payment payment = strategy.getPayment(null, serviceClient);

        assertEquals("Invalid payment", Payment.INVALID_PAYMENT, payment);
    }

}
