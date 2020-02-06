package io.singularitynet.sdk.client;

import io.grpc.Channel;
import java.util.function.Function;

import io.singularitynet.sdk.mpe.PaymentChannelManager;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.ethereum.Identity;

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
     * Return an instance of the payment channel manager.
     * @return payment channel manager instance.
     */
    PaymentChannelManager getPaymentChannelManager();

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
     * Closes platform service connection.
     */
    void shutdownNow();

}
