package io.singularitynet.sdk.client;

import java.util.function.Function;
import java.util.function.Consumer;
import java.math.BigInteger;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.daemon.DaemonConnection;
import io.singularitynet.sdk.daemon.Payment;
import io.singularitynet.sdk.registry.*;
import io.singularitynet.sdk.mpe.PaymentChannelProvider;
import io.singularitynet.sdk.mpe.PaymentChannel;
import io.singularitynet.sdk.mpe.MultiPartyEscrowContract;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Signer;

/**
 * The class is responsible for providing all necessary facilities to call
 * a platform service.
 */
public class BaseServiceClient implements ServiceClient {

    private final static Logger log = LoggerFactory.getLogger(BaseServiceClient.class);

    private final MultiPartyEscrowContract mpe;
    private final DaemonConnection daemonConnection;
    private final MetadataProvider metadataProvider;
    private final PaymentChannelProvider paymentChannelProvider;
    private final PaymentStrategy paymentStrategy;
    private final Signer signer;

    /**
     * Constructor.
     * @param mpe provides interface to MultiPartyEscrow contract.
     * @param daemonConnection provides live gRPC connection.
     * @param metadataProvider provides the service related metadata.
     * @param paymentChannelProvider provides the payment channel state.
     * @param paymentStrategy provides payment for the client call.
     * @param signer signs payments.
     */
    public BaseServiceClient(
            MultiPartyEscrowContract mpe,
            DaemonConnection daemonConnection,
            MetadataProvider metadataProvider,
            PaymentChannelProvider paymentChannelProvider,
            PaymentStrategy paymentStrategy,
            Signer signer) {
        this.mpe = mpe;
        this.daemonConnection = daemonConnection;
        this.daemonConnection.setClientCallsInterceptor(new PaymentClientInterceptor(this, paymentStrategy));
        this.metadataProvider = metadataProvider;
        this.paymentChannelProvider = paymentChannelProvider;
        this.paymentStrategy = paymentStrategy;
        this.signer = signer;
    }

    @Override
    public MetadataProvider getMetadataProvider() {
        return metadataProvider;
    }

    @Override
    public PaymentChannelProvider getPaymentChannelProvider() {
        return paymentChannelProvider;
    }

    @Override
    public Signer getSigner() {
        return signer;
    }

    @Override
    public <T> T getGrpcStub(Function<Channel, T> constructor) {
        return daemonConnection.getGrpcStub(constructor);
    }

    @Override
    public DaemonConnection getDaemonConnection() {
        return daemonConnection;
    }

    @Override
    public void shutdownNow() {
        daemonConnection.shutdownNow();
        log.info("Service client shutdown");
    }

    @Override
    public PaymentChannel openPaymentChannel(Signer signer,
            Function<EndpointGroup, BigInteger> valueExpr,
            Function<PaymentGroup, BigInteger> expirationExpr) {

        // FIXME: should it be the method parameter?
        String groupName = daemonConnection.getEndpointGroupName();

        EndpointGroup endpointGroup = metadataProvider.getServiceMetadata()
            .getEndpointGroupByName(groupName).get();
        PaymentGroup paymentGroup = metadataProvider.getOrganizationMetadata()
            .getPaymentGroupById(endpointGroup.getPaymentGroupId()).get();

        BigInteger value = valueExpr.apply(endpointGroup);

        Address recipient = paymentGroup.getPaymentDetails().getPaymentAddress();
        PaymentGroupId groupId = paymentGroup.getPaymentGroupId();
        BigInteger expiration = expirationExpr.apply(paymentGroup);

        PaymentChannel channel = mpe.openChannel(signer.getAddress(),
                recipient, groupId, value, expiration);

        return channel;
    }

    @Override
    public PaymentChannel addFundsToChannel(PaymentChannel channel, BigInteger amount) {
        BigInteger valueInc = mpe.channelAddFunds(channel.getChannelId(), amount);
        return channel.toBuilder()
            .setValue(channel.getValue().add(valueInc))
            .build();
    }

    @Override
    public PaymentChannel extendChannel(PaymentChannel channel, BigInteger expiration) {
        BigInteger newExpiration = mpe.channelExtend(channel.getChannelId(), expiration);
        return channel.toBuilder()
            .setExpiration(newExpiration)
            .build();
    }

    @Override
    public PaymentChannel extendAndAddFundsToChannel(PaymentChannel channel,
            BigInteger expiration, BigInteger amount) {
        MultiPartyEscrowContract.ExtendAndAddFundsResponse response = mpe
            .channelExtendAndAddFunds(channel.getChannelId(), expiration, amount);
        return channel.toBuilder()
            .setExpiration(response.expiration)
            .setValue(channel.getValue().add(response.valueIncrement))
            .build();
    }

    /**
     * Class responsibility is injecting payment information into gRPC metadata
     * before making remote gRPC call. It uses PaymentStrategy instance to
     * calculate payment and ClientCallWrapper instance to inject payment into
     * gRPC metadata.
     */
    private static class PaymentClientInterceptor implements ClientInterceptor {

        private final static Logger log = LoggerFactory.getLogger(PaymentClientInterceptor.class);

        private final ServiceClient serviceClient;
        private final PaymentStrategy paymentStrategy;

        public PaymentClientInterceptor(ServiceClient serviceClient, PaymentStrategy paymentStrategy) {
            this.serviceClient = serviceClient;
            this.paymentStrategy = paymentStrategy;
        }

        @Override
        public <ReqT,RespT> ClientCall<ReqT,RespT> interceptCall(
                MethodDescriptor<ReqT,RespT> method,
                CallOptions callOptions,
                Channel next) {
            log.debug("Calculating payment");
            final Payment payment = paymentStrategy.getPayment(
                    new GrpcCallParameters<>(method, callOptions, next),
                    serviceClient);
            log.debug("Payment calculated: {}", payment);
            return new ClientCallWrapper<>(next.newCall(method, callOptions),
                    headers -> payment.toMetadata(headers));
        }

    }

    /**
     * This class is io.grpc.ClientCall wrapper injecting custom gRPC metadata
     * before client call. Class overrides start() method and updates metadata
     * before passing it to wrapped io.grpc.ClientCall instance. This is the
     * only way of metadata injection in gRPC client call interceptor. 
     */
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
