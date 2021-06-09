package io.singularitynet.sdk.daemon;

import io.grpc.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.registry.MetadataProvider;

// @ThreadSafe
public class BaseDaemonConnection implements DaemonConnection {

    private final static Logger log = LoggerFactory.getLogger(BaseDaemonConnection.class);

    private final EndpointSelector endpointSelector;
    private final GrpcSettings grpcSettings;
    private final ClientInterceptorProxy interceptorProxy;
    private final MetadataProvider metadataProvider;

    private AtomicReference<ManagedChannel> channel = new AtomicReference<>();
    private volatile Endpoint endpoint;

    public BaseDaemonConnection(EndpointSelector endpointSelector,
            GrpcSettings grpcSettings, MetadataProvider metadataProvider) {
        log.info("New daemon connection, endpointSelector: {}", endpointSelector);
        this.endpointSelector = endpointSelector;
        this.grpcSettings = grpcSettings;
        this.interceptorProxy = new ClientInterceptorProxy();
        this.metadataProvider = metadataProvider;
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
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public void shutdownNow() {
        channel.get().shutdownNow();
        log.info("gRPC channel to daemon closed");
    }

    private ManagedChannel getChannelLazy() {
        ManagedChannel value = channel.get();
        if (value != null) {
            return value;
        }

        value = getChannel();
        if (channel.compareAndSet(null, value)) {
            return value;
        }
        value.shutdownNow();
        return channel.get();
    }
    
    // TODO: make this part of the configuration
    private static int MAX_GRPC_INBOUND_MESSAGE_SIZE = 1 << 24;

    private ManagedChannel getChannel() {
        endpoint = endpointSelector.nextEndpoint(metadataProvider);
        URL url = endpoint.getUrl();
        ManagedChannelBuilder builder = ManagedChannelBuilder
            .forAddress(url.getHost(), url.getPort())
            .maxInboundMessageSize(this.grpcSettings.getMaxInboundMessageSize())
            .intercept(interceptorProxy);
        // TODO: test HTTPS connections
        if ("http".equals(url.getProtocol())) {
            builder.usePlaintext();
        }
        ManagedChannel channel = builder.build();
        log.info("gRPC channel created, channel: {}", channel);
        return channel;
    }

    // @ThreadSafe
    private static class ClientInterceptorProxy implements ClientInterceptor {

        private final static Logger log = LoggerFactory.getLogger(ClientInterceptorProxy.class);

        private volatile ClientInterceptor delegate;

        public void setDelegate(ClientInterceptor delegate) {
            this.delegate = delegate;
        }

        private static final String PAYMENT_CHANNEL_STATE_SERVICE = "escrow.PaymentChannelStateService";
        private static final String FREE_CALL_STATE_SERVICE = "escrow.FreeCallStateService";
        private static final String PROVIDER_CONTROL_SERVICE = "escrow.ProviderControlService";

        @Override
        public <ReqT,RespT> ClientCall<ReqT,RespT> interceptCall(
                MethodDescriptor<ReqT,RespT> method,
                CallOptions callOptions,
                Channel next) {

            if (PAYMENT_CHANNEL_STATE_SERVICE.equals(getServiceName(method))) {
                log.debug("Skip processing for PaymentChannelStateService call");
                return next.newCall(method, callOptions);
            }

            if (FREE_CALL_STATE_SERVICE.equals(getServiceName(method))) {
                log.debug("Skip processing for FreeCallStateService call");
                return next.newCall(method, callOptions);
            }

            if (PROVIDER_CONTROL_SERVICE.equals(getServiceName(method))) {
                log.debug("Skip processing for ProviderControlService call");
                return next.newCall(method, callOptions);
            }

            log.debug("New gRPC call intercepted, method: {}, callOptions: {}, gRPC channel: {}", method, callOptions, next);
            return delegate.interceptCall(method, callOptions, next);
        }

    }

    private static String getServiceName(MethodDescriptor<?, ?> method) {
        return MethodDescriptor.extractFullServiceName(method.getFullMethodName());
    }

}
