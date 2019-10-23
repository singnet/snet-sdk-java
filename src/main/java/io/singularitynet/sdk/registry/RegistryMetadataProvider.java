package io.singularitynet.sdk.registry;

import com.google.gson.*;
import static io.singularitynet.sdk.registry.Utils.*;

public class RegistryMetadataProvider implements MetadataProvider {
        
	private final String orgId;
	private final String serviceId;
	private final RegistryContract registryContract;
	private final MetadataStorage metadataStorage;

    public RegistryMetadataProvider(String orgId, String serviceId,
            RegistryContract registryContract, MetadataStorage metadataStorage) {
		this.orgId = orgId;
		this.serviceId = serviceId;
		this.registryContract = registryContract;
		this.metadataStorage = metadataStorage;
    }

    @Override
    public ServiceMetadata getServiceMetadata() {
        ServiceRegistration registration = registryContract.getServiceRegistrationById(orgId, serviceId).get();
        byte[] metadataBytes = metadataStorage.get(registration.getMetadataUri());
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        return gson.fromJson(bytesToStr(metadataBytes), ServiceMetadata.class);
    }

}
