package io.singularitynet.sdk.test;

import org.junit.*;
import org.mockito.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import io.grpc.*;

import io.singularitynet.sdk.test.TestServiceGrpc.TestServiceBlockingStub;

public class GrpcTest {

    private TestServer server;
    private TestClientInterceptor clientInterceptor;
    private ManagedChannel channel;
    private TestServiceBlockingStub client;

    @Before
    public void setUp() {
        server = TestServer.start();
        URL url = server.getEndpoint();
        clientInterceptor = new TestClientInterceptor();
        channel = ManagedChannelBuilder
            .forAddress(url.getHost(), url.getPort())
            .intercept(clientInterceptor)
            .usePlaintext()
            .build();
        client = TestServiceGrpc.newBlockingStub(channel);
    }

    @After
    public void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    public void getServiceNameReturnsServiceName() {
        client.echo(Input.newBuilder().setInput("ping").build());
            
        assertEquals("Service name returned", "io.singularitynet.sdk.test.TestService",
                MethodDescriptor.extractFullServiceName(clientInterceptor.method.getFullMethodName()));
    }

    private static class TestClientInterceptor implements ClientInterceptor {

        private volatile MethodDescriptor method;

        @Override
        public <ReqT,RespT> ClientCall<ReqT,RespT> interceptCall(
                MethodDescriptor<ReqT,RespT> method,
                CallOptions callOptions,
                Channel next) {
            this.method = method;
            return next.newCall(method, callOptions);
        }

    }
}
