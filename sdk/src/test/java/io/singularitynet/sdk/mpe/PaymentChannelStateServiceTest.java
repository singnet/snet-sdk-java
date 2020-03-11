package io.singularitynet.sdk.mpe;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import com.google.protobuf.ByteString;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;
import io.singularitynet.sdk.daemon.GrpcUtils;
import io.singularitynet.sdk.daemon.DaemonConnection;
import io.singularitynet.daemon.escrow.StateService.ChannelStateRequest;

public class PaymentChannelStateServiceTest {

    @Test
    public void signChannelStateRequest() {
        String privateKey = "89765001819765816734960087977248703971879862101523844953632906408104497565820";
        BigInteger ethereumBlockNumber = BigInteger.valueOf(53);
        Address mpeAddress = new Address("0xf25186B5081Ff5cE73482AD761DB0eB0d25abfBF");
        long channelId = 42;

        Identity signer = new PrivateKeyIdentity(new BigInteger(privateKey));
        DaemonConnection connection = mock(DaemonConnection.class);
        when(connection.getLastEthereumBlockNumber()).thenReturn(ethereumBlockNumber);
        PaymentChannelStateService.MessageSigningHelper helper =
            new PaymentChannelStateService.MessageSigningHelper(mpeAddress, connection);
        ChannelStateRequest.Builder request = ChannelStateRequest.newBuilder()
            .setChannelId(GrpcUtils.toBytesString(BigInteger.valueOf(channelId)));

        helper.signChannelStateRequest(request, signer);

        assertEquals("Signature", "kegbvf4a+kzqDiIkDDsWIZu2EFqbR5dQzKrSmy3w6uxhg+NuOFc09wwXSwUiO46R5FN+XQ/Yjtwgxyck4K9OhRs=",
                Utils.bytesToBase64(request.getSignature().toByteArray()));
    }

}
