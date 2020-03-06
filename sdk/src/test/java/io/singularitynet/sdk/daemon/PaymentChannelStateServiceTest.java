package io.singularitynet.sdk.daemon;

import org.junit.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import com.google.protobuf.ByteString;

import io.singularitynet.sdk.test.Environment;
import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.daemon.escrow.StateService.ChannelStateRequest;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;

public class PaymentChannelStateServiceTest {

    private Environment env;

    @Before
    public void setUp() {
        env = Environment.env();
    }

    @Test
    public void signChannelStateRequest() {
        String privateKey = "89765001819765816734960087977248703971879862101523844953632906408104497565820";
        long ethereumBlockNumber = 53;
        Address mpeAddress = new Address("0xf25186B5081Ff5cE73482AD761DB0eB0d25abfBF");
        long channelId = 42;

        env.setCurrentEthereumBlockNumber(ethereumBlockNumber);
        Identity signer = new PrivateKeyIdentity(new BigInteger(privateKey));
        PaymentChannelStateService.MessageSigningHelper helper =
            new PaymentChannelStateService.MessageSigningHelper(mpeAddress, new Ethereum(env.web3j()));
        ChannelStateRequest.Builder request = ChannelStateRequest.newBuilder()
            .setChannelId(GrpcUtils.toBytesString(BigInteger.valueOf(channelId)));

        helper.signChannelStateRequest(request, signer);

        assertEquals("Signature", "kegbvf4a+kzqDiIkDDsWIZu2EFqbR5dQzKrSmy3w6uxhg+NuOFc09wwXSwUiO46R5FN+XQ/Yjtwgxyck4K9OhRs=",
                Utils.bytesToBase64(request.getSignature().toByteArray()));
    }

}
