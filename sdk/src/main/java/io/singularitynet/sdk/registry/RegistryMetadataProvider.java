package io.singularitynet.sdk.registry;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Type;
import java.util.List;
import java.net.URL;

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
    public OrganizationMetadata getOrganizationMetadata() {
        log.debug("Get organization metadata, orgId: {}", orgId);
        OrganizationRegistration registration = registryContract.getOrganizationById(orgId).get();
        byte[] metadataBytes = metadataStorage.get(registration.getMetadataUri());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(PaymentGroup.class, new PaymentGroupDeserializer());
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        Gson gson = gsonBuilder.create();

        OrganizationMetadata metadata = gson.fromJson(Utils.bytesToStr(metadataBytes), OrganizationMetadata.class);
        log.debug("Metadata received: {}", metadata);
        return metadata;
    }

    private static class PaymentGroupDeserializer implements JsonDeserializer<PaymentGroup> {

        @Override
        public PaymentGroup deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String paymentGroupId = context.deserialize(jsonObject.get("group_id"), String.class);
            return PaymentGroup.newBuilder()
                .setGroupName(jsonObject.get("group_name").getAsString())
                .setPaymentGroupId(new PaymentGroupId(paymentGroupId))
                .setPaymentDetails(context.deserialize(jsonObject.get("payment"),
                            PaymentDetails.class))
                .build();
        }

    }

    @Override
    public ServiceMetadata getServiceMetadata() {
        log.debug("Get service metadata, orgId: {}, serviceId: {}", orgId, serviceId);
        ServiceRegistration registration = registryContract.getServiceRegistrationById(orgId, serviceId).get();
        byte[] metadataBytes = metadataStorage.get(registration.getMetadataUri());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(EndpointGroup.class, new EndpointGroupDeserializer());
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        Gson gson = gsonBuilder.create();

        ServiceMetadata metadata = gson.fromJson(Utils.bytesToStr(metadataBytes), ServiceMetadata.class);
        log.debug("Metadata received: {}", metadata);
        return metadata;
    }

    private static class EndpointGroupDeserializer implements JsonDeserializer<EndpointGroup> {

        @Override
        public EndpointGroup deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String paymentGroupId = context.deserialize(jsonObject.get("group_id"), String.class);
            return EndpointGroup.newBuilder()
                .setGroupName(jsonObject.get("group_name").getAsString())
                .setPricing(context.deserialize(jsonObject.get("pricing"),
                            Utils.parameterizedType(List.class, null, Pricing.class)))
                .setEndpoints(context.deserialize(jsonObject.get("endpoints"),
                            Utils.parameterizedType(List.class, null, URL.class)))
                .setPaymentGroupId(new PaymentGroupId(paymentGroupId))
                .build();
        }

    }

}
