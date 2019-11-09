package io.singularitynet.sdk.mpe;

import static org.junit.Assert.*;
import org.junit.*;

import java.math.BigInteger;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;

public class EscrowPaymentTest {

    @Test
    public void messageSignedCorrectly() {
        PaymentChannel channel = PaymentChannel.newBuilder()
            .setMpeContractAddress(new Address("0xf25186B5081Ff5cE73482AD761DB0eB0d25abfBF"))
            .setChannelId(BigInteger.valueOf(42))
            .setNonce(BigInteger.valueOf(3))
            .build();
        PrivateKeyIdentity signer = new PrivateKeyIdentity(Utils.base64ToBytes("Bvk3Bf8PnVj6kwE1IrG/gHXUpYO+chDKf4mu1FTilkI="));

        EscrowPayment payment = EscrowPayment.newBuilder()
            .setPaymentChannel(channel)
            .setAmount(BigInteger.valueOf(12345))
            .setSigner(signer)
            .build();

        assertEquals("Escrow payment", "vupXehLD4yd+GhENa+slIPVsd2U8/V771TtlhiJE7YJ7jBM07ECTL8OEOb9C4BfFUEYmw2w2/7YcNAPOHOuKdBs=",
            Utils.bytesToBase64(payment.getSignature()));
    }

}
