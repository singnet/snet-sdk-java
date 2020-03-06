package io.singularitynet.sdk.daemon;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

import io.grpc.Channel;
import java.util.concurrent.CountDownLatch;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.ServiceMetadata;
import io.singularitynet.sdk.registry.EndpointGroup;

public class RandomEndpointDaemonConnectionTest {

    @Test
    public void getGrpcStubConcurrently() throws InterruptedException {
        final String endpointGroupName = "default_group";
        ServiceMetadata serviceMetadata = ServiceMetadata.newBuilder()
            .addEndpointGroup(EndpointGroup.newBuilder()
                    .setGroupName(endpointGroupName)
                    .addEndpoint(Utils.strToUrl("http://localhost:12345"))
                    .build())
            .build();
        MetadataProvider metadataProvider = mock(MetadataProvider.class);
        when(metadataProvider.getServiceMetadata()).thenReturn(serviceMetadata);
        RandomEndpointDaemonConnection connection = spy(
            new RandomEndpointDaemonConnection(endpointGroupName, metadataProvider, null));
        final CountDownLatch ready = new CountDownLatch(2);
        final CountDownLatch go = new CountDownLatch(1);
        doAnswer(new Answer() {

            public Object answer(InvocationOnMock invocation) throws Throwable {
                ready.countDown();
                go.await(); 
                return invocation.callRealMethod();
            }

        }).when(connection).getChannel();
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
