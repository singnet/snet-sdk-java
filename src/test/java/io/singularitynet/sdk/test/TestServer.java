package io.singularitynet.sdk.test;

import io.grpc.stub.StreamObserver;
import io.grpc.*;
import java.io.IOException;
import java.net.URL;

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

    public static class TestService extends TestServiceGrpc.TestServiceImplBase  {

        public void echo(Input input, StreamObserver<Output> callback) {
            Output output = Output.newBuilder()
                .setOutput(input.getInput())
                .build();
            callback.onNext(output);
            callback.onCompleted();
        }

    }

    public static class Daemon implements ServerInterceptor {

        public <ReqT,RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT,RespT> call, Metadata headers,
                ServerCallHandler<ReqT,RespT> next) {
            return next.startCall(call, headers);
        }

    }

}
