package io.singularitynet.sdk.test;

import io.grpc.stub.StreamObserver;
import io.grpc.*;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import io.singularitynet.daemon.escrow.*;
import io.singularitynet.sdk.mpe.*;

public class TestServer {

    private static final int RANDOM_AVAILABLE_PORT = 0;

    private final Server server;
    private final Daemon daemon;
    private final TestService testService;

    public TestServer(Server server, Daemon daemon, TestService testService) {
        this.server = server; 
        this.daemon = daemon;
        this.testService = testService;
    }

    public static TestServer start() {
        TestService service = new TestService();
        Daemon daemon = new Daemon();

        Server server = ServerBuilder
            .forPort(RANDOM_AVAILABLE_PORT)
            .addService(service)
            .intercept(daemon)
            .build();
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new TestServer(server, daemon, service);
    }

    public void shutdownNow() {
        server.shutdownNow();
    }

    public URL getEndpoint() {
        try {
            return new URL("http://localhost:" + String.valueOf(server.getPort()));
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public Daemon getDaemon() {
        return daemon;
    }

    public static class TestService extends TestServiceGrpc.TestServiceImplBase  {

        @Override
        public void echo(Input input, StreamObserver<Output> callback) {
            Output output = Output.newBuilder()
                .setOutput(input.getInput())
                .build();
            callback.onNext(output);
            callback.onCompleted();
        }

    }

    public static class Daemon extends PaymentChannelStateServiceGrpc.PaymentChannelStateServiceImplBase
            implements ServerInterceptor {

        private List<Payment> payments = Collections.synchronizedList(new ArrayList<>());

        @Override
        public <ReqT,RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT,RespT> call, Metadata headers,
                ServerCallHandler<ReqT,RespT> next) {
            Optional<Payment> payment = PaymentSerializer.fromMetadata(headers);
            if (payment.isPresent()) {
                payments.add(payment.get());
            }
            return next.startCall(call, headers);
        }

        public List<Payment> getPayments() {
            return payments;
        }

        @Override
        public void getChannelState(StateService.ChannelStateRequest request,
                StreamObserver<StateService.ChannelStateReply> responseObserver) {
        }

    }

}
