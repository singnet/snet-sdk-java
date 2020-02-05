package io.singularitynet.sdk.client;

import io.grpc.Channel;
import java.util.function.Function;
import java.math.BigInteger;

import io.singularitynet.sdk.mpe.PaymentChannelProvider;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.registry.PaymentGroup;
import io.singularitynet.sdk.registry.Pricing;
import io.singularitynet.sdk.registry.PriceModel;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.daemon.DaemonConnection;
import io.singularitynet.sdk.mpe.PaymentChannel;

/**
 * The interface provides all necessary facilities to work with the platform
 * service.
 */
public interface ServiceClient {

    /**
     * Return an instance of the metadata provider.
     * @return metadata provider instance.
     */
    MetadataProvider getMetadataProvider();

    /**
     * Return an instance of the payment channel provider.
     * @return payment channel provider instance.
     */
    PaymentChannelProvider getPaymentChannelProvider();

    // FIXME: this method can be removed and signer can be received from SDK
    // itself instead getting it from daemon.
    /**
     * Return identity to sign payments.
     */
    Identity getSigner();

    /**
     * Construct new gRPC stub to call the platform service.
     * @param <T> type of the gRPC service stub.
     * @param constructor constructs new gRPC stub from the passed gRPC
     * channel.
     * @return gRPC stub constracted.
     */
    <T> T getGrpcStub(Function<Channel, T> constructor);

    /**
     * Return current endpoint group name. Endpoint group name can be changed
     * after failover or reconnection.
     * @return name of the current endpoint group to which client  is
     * connected.
     */
    String getEndpointGroupName();

    /**
     * Closes platform service connection. This call causes calling
     * DaemonConnection.shutdownNow().
     */
    void shutdownNow();

    // FIXME: add javadoc
    PaymentChannel openPaymentChannel(Identity signer, BigInteger value,
            BigInteger expiration);

    //FIXME: find out proper place for the methods
    //FIXME: javadoc
    PaymentChannel addFundsToChannel(PaymentChannel channel, BigInteger amount);
    PaymentChannel extendChannel(PaymentChannel channel, BigInteger expiration);
    PaymentChannel extendAndAddFundsToChannel(PaymentChannel channel,
            BigInteger expiration, BigInteger amount);

}
