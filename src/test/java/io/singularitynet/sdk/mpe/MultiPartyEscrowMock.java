package io.singularitynet.sdk.mpe;

import java.math.BigInteger;
import org.web3j.protocol.core.*;
import org.web3j.tuples.generated.*;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import static org.mockito.Mockito.*;

import static io.singularitynet.sdk.registry.Utils.*;

public class MultiPartyEscrowMock {

    private final MultiPartyEscrow mpe = mock(MultiPartyEscrow.class);

    public MultiPartyEscrow get() {
        return mpe;
    }

    public void addPaymentChannel(PaymentChannel paymentChannel) {
        when(mpe.channels(eq(paymentChannel.getChannelId()))).
                thenReturn(new RemoteFunctionCall<>(null, () -> {
                    return new Tuple7<>(paymentChannel.getNonce(),
                            paymentChannel.getSender(),
                            paymentChannel.getSigner(),
                            paymentChannel.getRecipient(),
                            paymentChannel.getPaymentGroupId(),
                            paymentChannel.getValue(),
                            paymentChannel.getExpiration());
                }));
    }

    public void setContractAddress(String address) {
        when(mpe.getContractAddress()).thenReturn(address);
    }

}
