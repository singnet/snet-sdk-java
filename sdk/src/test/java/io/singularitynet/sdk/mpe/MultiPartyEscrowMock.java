package io.singularitynet.sdk.mpe;

import java.math.BigInteger;

import org.web3j.protocol.core.*;
import org.web3j.tuples.generated.*;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;

import static org.mockito.Mockito.*;

import io.singularitynet.sdk.ethereum.Address;

public class MultiPartyEscrowMock {

    private final MultiPartyEscrow mpe = mock(MultiPartyEscrow.class);

    public MultiPartyEscrow get() {
        return mpe;
    }

    public void addPaymentChannel(PaymentChannel paymentChannel) {
        RemoteFunctionCall<Tuple7<BigInteger, String, String, String, byte[], BigInteger, BigInteger>> remoteCall =
                mock(RemoteFunctionCall.class);
        try {
            when(remoteCall.send()).thenReturn(new Tuple7<>(paymentChannel.getNonce(),
                    paymentChannel.getSender().toString(),
                    paymentChannel.getSigner().toString(),
                    paymentChannel.getRecipient().toString(),
                    paymentChannel.getPaymentGroupId().getBytes(),
                    paymentChannel.getValue(),
                    paymentChannel.getExpiration()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(mpe.channels(eq(paymentChannel.getChannelId()))).
                thenReturn(remoteCall);

//        try {
//            when(remoteCall.send()).thenReturn(new Tuple6<>(
//                    true,
//                    Utils.strToBytes32(registration.getOrgId()),
//                    Utils.strToBytes(registration.getMetadataUri().toString()),
//                    "0xfA8a01E837c30a3DA3Ea862e6dB5C6232C9b800A",
//                    Collections.EMPTY_LIST,
//                    registration.getServiceIds().stream().map(Utils::strToBytes32).collect(toList())
//            ));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        when(registry.getOrganizationById(eq(Utils.strToBytes32(orgId))))
//                .thenReturn(remoteCall);
    }

    public void setContractAddress(Address address) {
        when(mpe.getContractAddress()).thenReturn(address.toString());
    }

}
