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
import io.singularitynet.sdk.registry.PaymentGroupId;

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

    @Test
    public void signFreeCallPayment() {
        PrivateKeyIdentity signer = new PrivateKeyIdentity(new BigInteger("29468014479921324926416335756462519929875592116482443327369636854796117545586"));

        FreeCallPayment payment = FreeCallPayment.newBuilder()
            .setSigner(signer)
            .setToken(FreeCallAuthToken.newBuilder()
                    .setDappUserId("user1")
                    .setExpirationBlock(new BigInteger("83081670000"))
                    .setToken("9ADB876B6607F574867C557087CB8DE57D3D5C5AC6D269804A56B3478529F804148467BFD94EEDFE649DBA17F16DD8FAE6D66A3300E1D22768FDCCFE9690F9A500")
                    .build())
            .setCurrentBlockNumber(new BigInteger("8308167"))
            .setOrgId("ExampleOrganizationId")
            .setServiceId("ExampleServiceId")
            .setPaymentGroupId(new PaymentGroupId("ISnvriUU/IQv5fQcVdT+eDpv6CgJx0tdDuB4KlvCPbA="))
            .build();

        assertEquals("Free call payment signature",
                new Signature(Utils.base64ToBytes("CnfbOozxd1PP643cDbcnY51jnGnHqSd75CcIal/gQotpYBHkmLKQOqGbL5DSPJMS0bWf9hQj2V4TNe42LEDc1Bs=")),
                payment.getSignature());
    }

}
