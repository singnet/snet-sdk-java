package io.singularitynet.sdk.daemon;

import io.grpc.*;
import java.net.URL;
import java.util.function.Function;

import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.ServiceMetadata;

public class FirstEndpointDaemonConnection implements DaemonConnection {

    private final String groupName;
    private final MetadataProvider metadataProvider;
    private final ClientInterceptorProxy interceptorProxy;

    private ManagedChannel channel;

    public FirstEndpointDaemonConnection(String groupName, MetadataProvider metadataProvider) {
        this.groupName = groupName;
        this.metadataProvider = metadataProvider;
        this.interceptorProxy = new ClientInterceptorProxy();
    }

    @Override
    public <T> T getGrpcStub(Function<Channel, T> constructor) {
        return constructor.apply(getChannelLazy());
    }

    @Override
    public void setClientCallsInterceptor(ClientInterceptor interceptor) {
        interceptorProxy.setDelegate(interceptor);
    }

    @Override
    public void shutdownNow() {
        channel.shutdownNow();
    }

    private ManagedChannel getChannelLazy() {
        // TODO: make thread safe
        if (channel == null) {
            channel = getChannel();
        }
        return channel;
    }

    private ManagedChannel getChannel() {
        ServiceMetadata serviceMetadata = metadataProvider.getServiceMetadata();
        URL url = serviceMetadata.getEndpointGroups().stream()
            .filter(group -> groupName.equals(group.getGroupName()))
            .findFirst().get().getEndpoints().get(0);
        ManagedChannelBuilder builder = ManagedChannelBuilder
            .forAddress(url.getHost(), url.getPort())
            .intercept(interceptorProxy);
        // FIXME: test HTTPS connections
        if ("http".equals(url.getProtocol())) {
            builder.usePlaintext();
        }
        return builder.build();
    }

    // ThreadSafe
    private static class ClientInterceptorProxy implements ClientInterceptor {

        private volatile ClientInterceptor delegate;

        public void setDelegate(ClientInterceptor delegate) {
            this.delegate = delegate;
        }

        private static final String PAYMENT_CHANNEL_STATE_SERVICE = "escrow.PaymentChannelStateService";

        @Override
        public <ReqT,RespT> ClientCall<ReqT,RespT> interceptCall(
                MethodDescriptor<ReqT,RespT> method,
                CallOptions callOptions,
                Channel next) {

            if (PAYMENT_CHANNEL_STATE_SERVICE.equals(getServiceName(method))) {
                return next.newCall(method, callOptions);
            }

            return delegate.interceptCall(method, callOptions, next);
        }

    }

    private static String getServiceName(MethodDescriptor<?, ?> method) {
        return MethodDescriptor.extractFullServiceName(method.getFullMethodName());
    }

}
