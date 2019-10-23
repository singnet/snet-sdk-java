package io.singularitynet.sdk.registry;

import org.web3j.protocol.core.*;
import org.web3j.tuples.generated.*;
import io.singularitynet.sdk.contracts.Registry;
import static org.mockito.Mockito.*;
import static java.util.stream.Collectors.toList;
import java.util.Collections;

import static io.singularitynet.sdk.common.Utils.*;
import io.singularitynet.sdk.common.Utils;

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

    public void addOrganizationRegistration(String orgId,
            OrganizationRegistration registration) {
        when(registry.getOrganizationById(eq(strToBytes32(orgId))))
            .thenReturn(new RemoteFunctionCall<>(null,
                        () -> {
                            return new Tuple7<>(true,
                                    strToBytes32(registration.getOrgId()),
                                    strToBytes(registration.getMetadataUri().toString()),
                                    "0xfA8a01E837c30a3DA3Ea862e6dB5C6232C9b800A",
                                    Collections.EMPTY_LIST,
                                    registration.getServiceIds().stream().map(Utils::strToBytes32).collect(toList()),
                                    Collections.EMPTY_LIST
                                    );
                        })
                    );
    }

}


