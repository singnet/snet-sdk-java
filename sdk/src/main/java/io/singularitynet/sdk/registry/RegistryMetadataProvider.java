package io.singularitynet.sdk.registry;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Type;
import java.util.List;
import java.net.URL;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;

/**
 * Metadata provider implementation which synchronously forwards calls to the
 * registry and metadata storage.
 */
public class RegistryMetadataProvider implements MetadataProvider {

    private final static Logger log = LoggerFactory.getLogger(RegistryMetadataProvider.class);
        
	private final String orgId;
	private final String serviceId;
	private final RegistryContract registryContract;
	private final MetadataStorage metadataStorage;

    /**
     * Constructor.
     * @param orgId organization id.
     * @param serviceId service id.
     * @param registryContract registry contract adapter.
     * @param metadataStorage metadata storage implementation.
     */
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
            EndpointGroup.Builder builder = EndpointGroup.newBuilder()
                .setGroupName(jsonObject.get("group_name").getAsString())
                .setPricing(context.deserialize(jsonObject.get("pricing"),
                            Utils.parameterizedType(List.class, null, Pricing.class)))
                .setEndpoints(context.deserialize(jsonObject.get("endpoints"),
                            Utils.parameterizedType(List.class, null, URL.class)))
                .setPaymentGroupId(new PaymentGroupId(paymentGroupId));

            if (jsonObject.has("free_calls")) {
                builder.setFreeCalls(jsonObject.get("free_calls").getAsLong());
            }

            if (jsonObject.has("free_call_signer_address")) {
                String freeCallSignerAddress = context.deserialize(jsonObject.get("free_call_signer_address"), String.class);
                builder.setFreeCallSignerAddress(new Address(freeCallSignerAddress));
            }

            return builder.build();
        }

    }

}
