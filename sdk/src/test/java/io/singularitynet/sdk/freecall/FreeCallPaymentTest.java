package io.singularitynet.sdk.freecall;

import org.junit.*;
import static org.junit.Assert.*;

import java.math.BigInteger;

import java.io.ByteArrayOutputStream;
import io.singularitynet.sdk.ethereum.Signature;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;

public class FreeCallPaymentTest {

    @Test
    public void generateFreeCallPaymentToken() {
        PrivateKeyIdentity signer = new PrivateKeyIdentity(new BigInteger("97086439711335761123927918050632077618411546417377476024996130587690333788989"));

        String freeCallToken = FreeCallPayment.generateFreeCallPaymentToken(
                "user1", new Address("0x8c2004d3510E6c01e9F51D3cE19F5bb94B0e6192"),
                new BigInteger("83081670000"), signer);

        assertEquals("Free call auth token (a signature)",
            new Signature(Utils.base64ToBytes("ISnvriUU/IQv5fQcVdT+eDpv6CgJx0tdDuB4KlvCPbBPV7Xvnfb/qGsbO9kof56O4KAuNMo5RiwiBBs7w51gzxs=")),
            new Signature(Utils.hexToBytes(freeCallToken)));
    }

}
