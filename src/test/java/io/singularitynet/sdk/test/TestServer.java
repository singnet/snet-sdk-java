package io.singularitynet.sdk.test;

import io.grpc.stub.StreamObserver;
import io.grpc.*;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import io.singularitynet.sdk.daemon.DaemonMock;

public class TestServer {

    private static final int RANDOM_AVAILABLE_PORT = 0;

    private final Server server;
    private final TestService testService;

    private TestServer(Server server, TestService testService) {
        this.server = server; 
        this.testService = testService;
    }

    public static TestServer start(DaemonMock daemon) {
        return startInternal(Optional.of(daemon));
    }

    public static TestServer startWithoutDaemon() {
        return startInternal(Optional.empty());
    }

    private static TestServer startInternal(Optional<DaemonMock> daemon) {
        TestService service = new TestService();

        ServerBuilder builder = ServerBuilder
            .forPort(RANDOM_AVAILABLE_PORT)
            .addService(service);
        if (daemon.isPresent()) {
            builder.addService(daemon.get()).intercept(daemon.get());
        }
        Server server = builder.build();
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new TestServer(server, service);
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

        @Override
        public void echo(Input input, StreamObserver<Output> callback) {
            Output output = Output.newBuilder()
                .setOutput(input.getInput())
                .build();
            callback.onNext(output);
            callback.onCompleted();
        }

    }

}
