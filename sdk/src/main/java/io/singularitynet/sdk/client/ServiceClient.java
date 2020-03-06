package io.singularitynet.sdk.client;

import io.grpc.Channel;
import java.util.function.Function;

import io.singularitynet.sdk.mpe.PaymentChannelStateProvider;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.daemon.FreeCallStateService;

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
     * Return actual payment channel state provider. As part of payment channel
     * state is kept on the daemon side the provider needs live connection to
     * the daemon.
     * @return actual payment channel state provider.
     */
    PaymentChannelStateProvider getPaymentChannelStateProvider();

    /**
     * Return a service instance to get free calls state.
     * @return free call service instance.
     */
    FreeCallStateService getFreeCallStateService();

    /**
     * Construct new gRPC stub to call the platform service.
     * @param <T> type of the gRPC service stub.
     * @param constructor constructs new gRPC stub from the passed gRPC
     * channel.
     * @return gRPC stub constracted.
     */
    <T> T getGrpcStub(Function<Channel, T> constructor);

    /**
     * Return organization id of the service.
     * @return organization id.
     */
    String getOrgId();

    /**
     * Return service id of the service.
     * @return service id.
     */
    String getServiceId();

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
