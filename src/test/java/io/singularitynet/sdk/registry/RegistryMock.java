package io.singularitynet.sdk.registry;

import org.web3j.protocol.core.*;
import org.web3j.tuples.generated.*;
import io.singularitynet.sdk.contracts.Registry;
import static org.mockito.Mockito.*;
import static java.util.stream.Collectors.toList;

import static io.singularitynet.sdk.registry.Utils.*;
import io.singularitynet.sdk.registry.Utils;

public class RegistryMock {

    private final Registry registry = mock(Registry.class);

    public Registry get() {
        return registry;
    }

    public void addServiceRegistration(String orgId, String serviceId,
            ServiceRegistration registration) {
        when(registry.getServiceRegistrationById(eq(strToBytes32(orgId)),
                    eq(strToBytes32(serviceId))))
            .thenReturn(new RemoteFunctionCall<>(null,
                        () -> {
                            return new Tuple4<>(true,
                                    strToBytes32(registration.getServiceId()),
                                    strToBytes(registration.getMetadataUri().toString()),
                                    registration.getTags().stream().map(Utils::strToBytes32).collect(toList()));
                        })
                    );
    }

}


