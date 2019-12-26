package io.singularitynet.sdk.registry;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.common.Utils;

public class RegistryMetadataProvider implements MetadataProvider {

    private final static Logger log = LoggerFactory.getLogger(RegistryMetadataProvider.class);
        
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
        log.debug("Get service metadata, orgId: {}, serviceId: {}", orgId, serviceId);
        ServiceRegistration registration = registryContract.getServiceRegistrationById(orgId, serviceId).get();
        byte[] metadataBytes = metadataStorage.get(registration.getMetadataUri());
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        ServiceMetadata metadata = gson.fromJson(Utils.bytesToStr(metadataBytes), ServiceMetadata.class);
        log.debug("Metadata received: {}", metadata);
        return metadata;
    }

}
