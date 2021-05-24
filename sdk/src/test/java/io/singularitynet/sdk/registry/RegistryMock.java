package io.singularitynet.sdk.registry;

import org.web3j.protocol.core.*;
import org.web3j.tuples.generated.*;
import io.singularitynet.sdk.contracts.Registry;
import static org.mockito.Mockito.*;
import static java.util.stream.Collectors.toList;
import java.util.Collections;

import io.singularitynet.sdk.common.Utils;

public class RegistryMock {

    private final Registry registry = mock(Registry.class);

    public Registry get() {
        return registry;
    }

    public void addServiceRegistration(String orgId, String serviceId,
            ServiceRegistration registration) {
        when(registry.getServiceRegistrationById(eq(Utils.strToBytes32(orgId)),
                    eq(Utils.strToBytes32(serviceId))))
            .thenReturn(new RemoteCall<>(
                        () -> {
                            return new Tuple3<>(true,
                                    Utils.strToBytes32(registration.getServiceId()),
                                    Utils.strToBytes(registration.getMetadataUri().toString())
                                    );
                        })
                    );
    }

    public void addOrganizationRegistration(String orgId,
            OrganizationRegistration registration) {
        when(registry.getOrganizationById(eq(Utils.strToBytes32(orgId))))
            .thenReturn(new RemoteCall<>(
                        () -> {
                            return new Tuple6<>(true,
                                    Utils.strToBytes32(registration.getOrgId()),
                                    Utils.strToBytes(registration.getMetadataUri().toString()),
                                    "0xfA8a01E837c30a3DA3Ea862e6dB5C6232C9b800A",
                                    Collections.EMPTY_LIST,
                                    registration.getServiceIds().stream().map(Utils::strToBytes32).collect(toList())
                                    );
                        })
                    );
    }

}


