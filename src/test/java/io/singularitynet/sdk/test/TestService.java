package io.singularitynet.sdk.test;

import io.grpc.stub.StreamObserver;
import io.grpc.ServerBuilder;
import io.grpc.Server;
import java.io.IOException;

public class TestService extends TestServiceGrpc.TestServiceImplBase {

    public static Server start(int port) {
        Server server = ServerBuilder
            .forPort(port)
            .addService(new TestService())
            .build();
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return server;
    }

    public void echo(Input input, StreamObserver<Output> callback) {
        Output output = Output.newBuilder()
            .setOutput(input.getInput())
            .build();
        callback.onNext(output);
        callback.onCompleted();
    }
    
}
