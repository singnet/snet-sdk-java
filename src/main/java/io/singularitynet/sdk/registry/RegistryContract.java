package io.singularitynet.sdk.registry;

import java.util.Optional;

public interface RegistryContract {

    Optional<ServiceRegistration> getServiceRegistrationById(String orgId, String serviceId);

}
