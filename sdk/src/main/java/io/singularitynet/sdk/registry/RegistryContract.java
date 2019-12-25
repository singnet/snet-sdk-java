package io.singularitynet.sdk.registry;

import java.util.Optional;
import org.web3j.tuples.generated.*;
import java.net.URI;
import java.util.List;
import io.singularitynet.sdk.contracts.Registry;

import static io.singularitynet.sdk.common.Utils.*;

public class RegistryContract {

    private final Registry registry;

    public RegistryContract(Registry registry) {
        this.registry = registry;
    }

    public Optional<OrganizationRegistration> getOrganizationById(String orgId) {
        return wrapExceptions(() -> {
            Tuple7<Boolean, byte[], byte[], String, List<String>, List<byte[]>, List<byte[]>> result =
                registry.getOrganizationById(strToBytes32(orgId)).send();
            OrganizationRegistration.Builder builder = OrganizationRegistration.newBuilder()
                .setOrgId(bytes32ToStr(result.getValue2()))
                .setMetadataUri(new URI(bytesToStr(result.getValue3())));
            for (byte[] serviceId : result.getValue6()) {
                builder.addServiceId(bytes32ToStr(serviceId));
            }
            // TODO: empty result case
            return Optional.of(builder.build());
        });
    }

    public Optional<ServiceRegistration> getServiceRegistrationById(String orgId, String serviceId) {
        return wrapExceptions(() -> {
            Tuple4<Boolean, byte[], byte[], List<byte[]>> result = 
                registry.getServiceRegistrationById(strToBytes32(orgId), strToBytes32(serviceId)).send();
            // TODO: empty result case
            return Optional.of(ServiceRegistration.newBuilder()
                    .setServiceId(bytes32ToStr(result.getValue2()))
                    .setMetadataUri(new URI(bytesToStr(result.getValue3())))
                    .build());
        });
    }

}
