package io.singularitynet.sdk.registry;

import java.util.Optional;
import org.web3j.tuples.generated.*;
import java.net.URI;
import java.util.List;
import io.singularitynet.sdk.contracts.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.singularitynet.sdk.common.Utils.*;

/**
 * Class adapts web3j generated contract ABI to the SDK data structures.
 */
public class RegistryContract {

    private final static Logger log = LoggerFactory.getLogger(RegistryContract.class);

    private final Registry registry;

    /**
     * New adapter from web3j generated contract.
     * @param registry web3j generated contract.
     */
    public RegistryContract(Registry registry) {
        this.registry = registry;
    }

    /**
     * Get organization registration information from Ethereum blockchain.
     * @param orgId id of the organization.
     * @return organization registration info.
     */
    public Optional<OrganizationRegistration> getOrganizationById(String orgId) {
        return wrapExceptions(() -> {
            log.info("Get organization from Registry, orgId: {}", orgId);
            Tuple6<Boolean, byte[], byte[], String, List<String>, List<byte[]>> result =
                registry.getOrganizationById(strToBytes32(orgId)).send();
            if (result.getValue1()) {
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
            } else {
                log.info("Organization registration record not found, orgId: {}", orgId);
                return Optional.empty();
            }
        });
    }

    /**
     * Get service registration information from Ethereum blockchain.
     * @param orgId organization id.
     * @param serviceId service id.
     * @return service registration information.
     */
    public Optional<ServiceRegistration> getServiceRegistrationById(String orgId, String serviceId) {
        return wrapExceptions(() -> {
            log.info("Get service from Registry, orgId: {}, serviceId: {}", orgId, serviceId);
            Tuple3<Boolean, byte[], byte[]> result = 
                registry.getServiceRegistrationById(strToBytes32(orgId), strToBytes32(serviceId)).send();
            if (result.getValue1()) {
                ServiceRegistration registration = ServiceRegistration.newBuilder()
                        .setServiceId(bytes32ToStr(result.getValue2()))
                        .setMetadataUri(new URI(bytesToStr(result.getValue3())))
                        .build();
                log.info("Service registration record received: {}", registration);
                return Optional.of(registration);
            } else {
                log.info("Service registration record not found, orgId: {}, serviceId: {}", orgId, serviceId);
                return Optional.empty();
            }
        });
    }

}
