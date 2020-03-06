package io.singularitynet.sdk.daemon;

import java.math.BigInteger;
import java.util.function.Function;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;

/**
 * Interface wraps gRPC connection to the service daemon.
 */
public interface DaemonConnection {

    /**
     * Construct new gRPC stub to call the platform service.
     * @param <T> type of the gRPC service stub.
     * @param constructor constructs new gRPC stub from the passed gRPC
     * channel.
     * @return gRPC stub constracted.
     */
    <T> T getGrpcStub(Function<Channel, T> constructor);

    /**
     * Set gRPC interceptor which is called before each service call.
     * Main purpose of the interceptor is to provide payment for the call.
     * @param interceptor interceptor instance.
     */
    void setClientCallsInterceptor(ClientInterceptor interceptor);

    /**
     * Return the name of the current endpoint group to which the connection is
     * opened. If implementation sticks to the same endpoint group then method
     * returns same value each time. If implementation supports failover
     * between endpoint groups then the name of the group can be changed after
     * failover happened.
     * @see io.singularitynet.sdk.registry.EndpointGroup#getGroupName
     * @return name of the endpoint group
     */
    String getEndpointGroupName();

    /**
     * Return last Ethereum block number for authentication.
     * @return last ethereum block number.
     */
    BigInteger getLastEthereumBlockNumber();

    /**
     * Closes platform service connection. This call causes calling
     * shutdownNow() on each stub returned by getGrpcStub() method.
     */
    void shutdownNow();

}
