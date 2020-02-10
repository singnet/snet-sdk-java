package io.singularitynet.sdk.client;

import io.grpc.MethodDescriptor;
import io.grpc.CallOptions;
import io.grpc.Channel;

/**
 * gRPC call parameters container to pass into client call interceptor.
 * @param <ReqT> type of the gRPC request of the call.
 * @param <RespT> type of the gRPC response of the call.
 */
public class GrpcCallParameters<ReqT, RespT> {

	private final MethodDescriptor<ReqT, RespT> method;
	private final CallOptions callOptions;
	private final Channel channel;

    /**
     * Constructor.
     * @param method gRPC method to be called.
     * @param callOptions call options.
     * @param channel gRPC channel to be used for the call.
     */
    public GrpcCallParameters(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel channel) {
		this.method = method;
		this.callOptions = callOptions;
		this.channel = channel;
    }

    /**
     * @return gRPC method to be called.
     */
    public MethodDescriptor<ReqT, RespT> getMethod() {
        return method;
    }

    /**
     * @return call options.
     */
    public CallOptions getCallOptions() {
        return callOptions;
    }

    /**
     * @return gRPC channel to be used for the call.
     */
    public Channel getChannel() {
        return channel;
    }

}
