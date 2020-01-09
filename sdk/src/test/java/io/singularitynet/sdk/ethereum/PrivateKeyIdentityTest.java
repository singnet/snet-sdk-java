package io.singularitynet.sdk.ethereum;

import org.junit.*;
import static org.junit.Assert.*;

import io.singularitynet.sdk.common.Utils;

public class PrivateKeyIdentityTest {

    @Test
    public void signPayment() {
        PrivateKeyIdentity identity = new PrivateKeyIdentity(Utils.base64ToBytes("1PeCDRD7vLjqiGoHl7A+yPuJIy8TdbNc1vxOyuPjxBM="));

        Signature signature = identity.sign(Utils.base64ToBytes("X19NUEVfY2xhaW1fbWVzc2FnZfJRhrUIH/XOc0gq12HbDrDSWr+/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADA5"));

        assertEquals("Message signature",
                new Signature(Utils.base64ToBytes("1uz5pkIREtQ9egzhv8khEmqQPTtu3EjYBHpllmBlnVAOU+MxK3vi6U7ECaFNkEJA0Bv3bqIGraaKOLwbJHwsFBw=")),
                signature);
    }

}
