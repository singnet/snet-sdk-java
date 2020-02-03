package io.singularitynet.sdk.daemon;

import io.grpc.*;
import java.net.URL;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.ServiceMetadata;

public class RandomEndpointDaemonConnection implements DaemonConnection {

    private final static Logger log = LoggerFactory.getLogger(RandomEndpointDaemonConnection.class);

    private final String groupName;
    private final MetadataProvider metadataProvider;
    private final ClientInterceptorProxy interceptorProxy;

    private ManagedChannel channel;

    public RandomEndpointDaemonConnection(String groupName, MetadataProvider metadataProvider) {
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
    public String getEndpointGroupName() {
        return groupName;
    }

    @Override
    public void shutdownNow() {
        channel.shutdownNow();
        log.info("gRPC channel to daemon closed");
    }

    private ManagedChannel getChannelLazy() {
        // TODO: make thread safe
        if (channel == null) {
            channel = getChannel();
        }
        return channel;
    }
    
    // TODO: make this part of the configuration
    private static int MAX_GRPC_INBOUND_MESSAGE_SIZE = 1 << 24;

    private ManagedChannel getChannel() {
        ServiceMetadata serviceMetadata = metadataProvider.getServiceMetadata();
        List<URL> urls = serviceMetadata.getEndpointGroups().stream()
            .filter(group -> groupName.equals(group.getGroupName()))
            .findFirst().get().getEndpoints();
        URL url = Utils.getRandomItem(urls);
        ManagedChannelBuilder builder = ManagedChannelBuilder
            .forAddress(url.getHost(), url.getPort())
            .maxInboundMessageSize(MAX_GRPC_INBOUND_MESSAGE_SIZE)
            .intercept(interceptorProxy);
        // TODO: test HTTPS connections
        if ("http".equals(url.getProtocol())) {
            builder.usePlaintext();
        }
        ManagedChannel channel = builder.build();
        log.info("gRPC channel created, channel: {}", channel);
        return channel;
    }

    // ThreadSafe
    private static class ClientInterceptorProxy implements ClientInterceptor {

        private final static Logger log = LoggerFactory.getLogger(ClientInterceptorProxy.class);

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
                log.debug("Skip processing for PaymentChannelStateService call");
                return next.newCall(method, callOptions);
            }

            log.debug("New gRPC call intercepted, method: {}, callOptions: {}", method, callOptions);
            return delegate.interceptCall(method, callOptions, next);
        }

    }

    private static String getServiceName(MethodDescriptor<?, ?> method) {
        return MethodDescriptor.extractFullServiceName(method.getFullMethodName());
    }

}
