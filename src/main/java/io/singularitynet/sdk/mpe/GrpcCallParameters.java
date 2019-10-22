package io.singularitynet.sdk.mpe;

import io.grpc.MethodDescriptor;
import io.grpc.CallOptions;
import io.grpc.Channel;

public class GrpcCallParameters<ReqT, RespT> {

	private final MethodDescriptor<ReqT, RespT> method;
	private final CallOptions callOptions;
	private final Channel channel;

    public GrpcCallParameters(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel channel) {
		this.method = method;
		this.callOptions = callOptions;
		this.channel = channel;
    }

    public MethodDescriptor<ReqT, RespT> getMethod() {
        return method;
    }

    public CallOptions getCallOptions() {
        return callOptions;
    }

    public Channel getChannel() {
        return channel;
    }

}
