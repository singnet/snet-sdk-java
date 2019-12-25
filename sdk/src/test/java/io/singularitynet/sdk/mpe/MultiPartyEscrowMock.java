package io.singularitynet.sdk.mpe;

import java.math.BigInteger;
import org.web3j.protocol.core.*;
import org.web3j.tuples.generated.*;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import static org.mockito.Mockito.*;

import static io.singularitynet.sdk.common.Utils.*;
import io.singularitynet.sdk.ethereum.Address;

public class MultiPartyEscrowMock {

    private final MultiPartyEscrow mpe = mock(MultiPartyEscrow.class);

    public MultiPartyEscrow get() {
        return mpe;
    }

    public void addPaymentChannel(PaymentChannel paymentChannel) {
        when(mpe.channels(eq(paymentChannel.getChannelId()))).
                thenReturn(new RemoteCall<>(() -> {
                    return new Tuple7<>(paymentChannel.getNonce(),
                            paymentChannel.getSender().toString(),
                            paymentChannel.getSigner().toString(),
                            paymentChannel.getRecipient().toString(),
                            paymentChannel.getPaymentGroupId(),
                            paymentChannel.getValue(),
                            paymentChannel.getExpiration());
                }));
    }

    public void setContractAddress(Address address) {
        when(mpe.getContractAddress()).thenReturn(address.toString());
    }

}
