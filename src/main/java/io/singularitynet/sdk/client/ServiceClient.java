package io.singularitynet.sdk.client;

import io.grpc.Channel;
import java.util.function.Function;

import io.singularitynet.sdk.mpe.PaymentChannelProvider;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.ethereum.Signer;

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

    /**
     * Return the signer to sign payments.
     */
    Signer getSigner();

    /**
     * Construct new gRPC stub to call the platform service.
     * @param <T> type of the gRPC service stub.
     * @param constructor constructs new gRPC stub from the passed gRPC
     * channel.
     * @return gRPC stub constracted.
     */
    <T> T getGrpcStub(Function<Channel, T> constructor);

    /**
     * Closes platform service connection. This call causes calling
     * DaemonConnection.shutdownNow().
     */
    void shutdownNow();


}
