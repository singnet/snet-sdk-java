package io.singularitynet.sdk.registry;

import java.util.Optional;
import org.web3j.tuples.generated.*;
import java.net.URI;
import java.util.List;
import io.singularitynet.sdk.contracts.Registry;

import static io.singularitynet.sdk.registry.Utils.*;

public class RegistryContract {

    private final Registry registry;

    public RegistryContract(Registry registry) {
        this.registry = registry;
    }

    public Optional<ServiceRegistration> getServiceRegistrationById(String orgId, String serviceId) {
        return rethrowChecked(() -> {
            Tuple4<Boolean, byte[], byte[], List<byte[]>> result = 
                registry.getServiceRegistrationById(strToBytes32(orgId), strToBytes32(serviceId)).send();
            return Optional.of(ServiceRegistration.newBuilder()
                    .setServiceId(bytes32ToStr(result.component2()))
                    .setMetadataUri(new URI(bytesToStr(result.component3())))
                    .build());
        });
    }

}
