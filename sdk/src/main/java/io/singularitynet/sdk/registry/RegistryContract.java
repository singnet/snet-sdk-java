package io.singularitynet.sdk.registry;

import java.util.Optional;
import org.web3j.tuples.generated.*;
import java.net.URI;
import java.util.List;
import io.singularitynet.sdk.contracts.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.singularitynet.sdk.common.Utils.*;

public class RegistryContract {

    private final static Logger log = LoggerFactory.getLogger(RegistryContract.class);

    private final Registry registry;

    public RegistryContract(Registry registry) {
        this.registry = registry;
    }

    public Optional<OrganizationRegistration> getOrganizationById(String orgId) {
        return wrapExceptions(() -> {
            log.info("Get organization from Registry, orgId: {}", orgId);
            Tuple7<Boolean, byte[], byte[], String, List<String>, List<byte[]>, List<byte[]>> result =
                registry.getOrganizationById(strToBytes32(orgId)).send();
            OrganizationRegistration.Builder builder = OrganizationRegistration.newBuilder()
                .setOrgId(bytes32ToStr(result.getValue2()))
                .setMetadataUri(new URI(bytesToStr(result.getValue3())));
            for (byte[] serviceId : result.getValue6()) {
                builder.addServiceId(bytes32ToStr(serviceId));
            }
            OrganizationRegistration registration = builder.build();
            log.info("Organization registration record received: {}", registration);
            // TODO: empty result case
            return Optional.of(registration);
        });
    }

    public Optional<ServiceRegistration> getServiceRegistrationById(String orgId, String serviceId) {
        return wrapExceptions(() -> {
            log.info("Get service from Registry, orgId: {}, serviceId: {}", orgId, serviceId);
            Tuple4<Boolean, byte[], byte[], List<byte[]>> result = 
                registry.getServiceRegistrationById(strToBytes32(orgId), strToBytes32(serviceId)).send();
            ServiceRegistration registration = ServiceRegistration.newBuilder()
                    .setServiceId(bytes32ToStr(result.getValue2()))
                    .setMetadataUri(new URI(bytesToStr(result.getValue3())))
                    .build();
            log.info("Service registration record received: {}", registration);
            // TODO: empty result case
            return Optional.of(registration);
        });
    }

}
