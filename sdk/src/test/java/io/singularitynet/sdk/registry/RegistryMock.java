package io.singularitynet.sdk.registry;

import org.web3j.protocol.core.*;
import org.web3j.tuples.generated.*;

import static org.mockito.Mockito.*;
import static java.util.stream.Collectors.toList;
import java.util.Collections;
import java.util.List;
import io.singularitynet.sdk.contracts.Registry;

import io.singularitynet.sdk.common.Utils;

public class RegistryMock {

    private final Registry registry = mock(Registry.class);

    public Registry get() {
        return registry;
    }

    public void addServiceRegistration(String orgId, String serviceId,
            ServiceRegistration registration) {
        RemoteFunctionCall<Tuple3<Boolean, byte[], byte[]>> remoteCall = mock(RemoteFunctionCall.class);
        try {
            when(remoteCall.send()).thenReturn(new Tuple3<>(
                    true,
                    Utils.strToBytes32(registration.getServiceId()),
                    Utils.strToBytes(registration.getMetadataUri().toString())
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(registry.getServiceRegistrationById(eq(Utils.strToBytes32(orgId)),
                eq(Utils.strToBytes32(serviceId))))
                .thenReturn(remoteCall);

    }

    public void addOrganizationRegistration(String orgId,
            OrganizationRegistration registration) {
        RemoteFunctionCall<Tuple6<Boolean, byte[], byte[], String, List<String>, List<byte[]>>> remoteCall =
            mock(RemoteFunctionCall.class);
        try {
            when(remoteCall.send()).thenReturn(new Tuple6<>(
                    true,
                    Utils.strToBytes32(registration.getOrgId()),
                    Utils.strToBytes(registration.getMetadataUri().toString()),
                    "0xfA8a01E837c30a3DA3Ea862e6dB5C6232C9b800A",
                    Collections.EMPTY_LIST,
                    registration.getServiceIds().stream().map(Utils::strToBytes32).collect(toList())
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(registry.getOrganizationById(eq(Utils.strToBytes32(orgId))))
            .thenReturn(remoteCall);
    }

}


