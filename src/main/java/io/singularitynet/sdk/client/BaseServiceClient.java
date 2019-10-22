package io.singularitynet.sdk.client;

import io.grpc.*;
import java.util.function.Function;
import java.net.URL;
import java.util.function.Consumer;

import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.ServiceMetadata;
import io.singularitynet.sdk.mpe.PaymentStrategy;
import io.singularitynet.sdk.mpe.Payment;
import io.singularitynet.sdk.mpe.GrpcCallParameters;
import io.singularitynet.sdk.mpe.PaymentSerializer;

public class BaseServiceClient implements ServiceClient {

    private final MetadataProvider metadataProvider;
    private final PaymentStrategy paymentStrategy;

    private ManagedChannel channel;

    public BaseServiceClient(MetadataProvider metadataProvider, PaymentStrategy paymentStrategy) {
        this.metadataProvider = metadataProvider;
        this.paymentStrategy = paymentStrategy;
    }

    @Override
    public <T> T getGrpcStub(Function<Channel, T> constructor) {
        return constructor.apply(getChannelLazy());
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
        URL url = serviceMetadata.getEndpointGroups().get(0).getEndpoints().get(0);
        return ManagedChannelBuilder
            .forAddress(url.getHost(), url.getPort())
            .intercept(new PaymentClientInterceptor(paymentStrategy))
            // TODO: support TLS connections
            .usePlaintext()
            .build();
    }

    private static class PaymentClientInterceptor implements ClientInterceptor {

        private final PaymentStrategy paymentStrategy;

        public PaymentClientInterceptor(PaymentStrategy paymentStrategy) {
            this.paymentStrategy = paymentStrategy;
        }

        @Override
        public <ReqT,RespT> ClientCall<ReqT,RespT> interceptCall(
                MethodDescriptor<ReqT,RespT> method,
                CallOptions callOptions,
                Channel next) {
            final Payment payment = paymentStrategy.getPayment(
                    new GrpcCallParameters<>(method, callOptions, next));
            return new ClientCallWrapper<>(next.newCall(method, callOptions),
                    headers -> PaymentSerializer.toMetadata(payment, headers));
        }

    }

    private static class ClientCallWrapper<ReqT, RespT> extends ClientCall<ReqT, RespT> {

        private final ClientCall<ReqT, RespT> delegate;
        private Consumer<Metadata> metadataUpdater;

        public ClientCallWrapper(ClientCall<ReqT, RespT> delegate,
                Consumer<Metadata> metadataUpdater) {
            this.delegate = delegate;
            this.metadataUpdater = metadataUpdater;
        }

        @Override
        public void cancel(String message, Throwable cause) {
            delegate.cancel(message, cause);
        }

        @Override
        public Attributes getAttributes() {
            return delegate.getAttributes();
        }

        @Override
        public void halfClose() {
            delegate.halfClose();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void request(int numMessages) {
            delegate.request(numMessages);
        }

        @Override
        public void sendMessage(ReqT message) {
            delegate.sendMessage(message);
        }

        @Override
        public void setMessageCompression(boolean enabled) {
            delegate.setMessageCompression(enabled);
        }

        @Override
        public void start(ClientCall.Listener<RespT> responseListener, Metadata headers) {
            metadataUpdater.accept(headers);
            delegate.start(responseListener, headers);
        }

    }

}
