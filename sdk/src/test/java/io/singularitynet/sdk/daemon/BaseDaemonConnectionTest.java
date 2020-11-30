package io.singularitynet.sdk.daemon;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.grpc.Channel;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.registry.EndpointGroup;
import io.singularitynet.sdk.registry.MetadataProvider;

public class BaseDaemonConnectionTest {

    @Test
    public void getGrpcStubConcurrently() throws InterruptedException {
        final CountDownLatch ready = new CountDownLatch(2);
        final CountDownLatch go = new CountDownLatch(1);
        EndpointSelector strategy = new EndpointSelector() {

            public Endpoint nextEndpoint(MetadataProvider metadataProvider) {
                return Utils.wrapExceptions(() -> {
                    ready.countDown();
                    go.await(); 
                    return new Endpoint() {

                        public EndpointGroup getGroup() {
                            return EndpointGroup.newBuilder()
                                .setGroupName("default_group")
                                .addEndpoint(Utils.strToUrl("http://localhost:12345"))
                                .build();
                        }

                        public URL getUrl() {
                            return Utils.strToUrl("http://localhost:12345");
                        }

                    };
                });
            }

        };
        BaseDaemonConnection connection = 
            new BaseDaemonConnection(strategy, null);
        final Channel[] channels = new Channel[2];
        Thread threadA = new Thread(() -> {
            connection.getGrpcStub(channel -> channels[0] = channel);
        });
        Thread threadB = new Thread(() -> {
            connection.getGrpcStub(channel -> channels[1] = channel);
        });

        threadA.start();
        threadB.start();
        ready.await();
        go.countDown();
        threadA.join();
        threadB.join();

        assertNotNull("Channels received are not null", channels[0]);
        assertNotNull("Channels received are not null", channels[1]);
        assertTrue("Channels received must be equal", channels[0] == channels[1]);
    }

}
