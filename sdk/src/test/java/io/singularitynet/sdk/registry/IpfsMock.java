package io.singularitynet.sdk.registry;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import javax.json.*;
import static org.mockito.Mockito.*;
import java.net.URI;
import java.net.URL;

import static io.singularitynet.sdk.common.Utils.*;

public class IpfsMock {

    private final IPFS ipfs = mock(IPFS.class);

    public IPFS get() {
        return ipfs;
    }

    public URI addService(ServiceMetadata metadata) {
        JsonObjectBuilder rootBuilder = Json.createObjectBuilder()
            .add("version", "1")
            .add("display_name", metadata.getDisplayName())
            .add("encoding", "proto")
            .add("service_type", "grpc")
            .add("model_ipfs_hash", "QmR3anSdm4s13iLt3zzyrSbtvCDJNwhkrYG6yFGFHXBznb")
            .add("mpe_address", metadata.getMpeAddress().toString());
        JsonArrayBuilder groupsBuilder = Json.createArrayBuilder();
        for (EndpointGroup group : metadata.getEndpointGroups()) {
            JsonObjectBuilder groupBuilder = Json.createObjectBuilder()
                .add("group_name", group.getGroupName());

            JsonArrayBuilder pricingBuilder = Json.createArrayBuilder();
            boolean dflt = true;
            for (Pricing price : group.getPricing()) {
                JsonObjectBuilder priceBuilder = Json.createObjectBuilder()
                    .add("price_model", price.getPriceModel().toString().toLowerCase())
                    .add("price_in_cogs", price.getPriceInCogs().toString());
                if (dflt) {
                    priceBuilder.add("default", true);
                    dflt = false;
                }
                pricingBuilder.add(priceBuilder);
            }
            groupBuilder.add("pricing", pricingBuilder);

            JsonArrayBuilder endpointsBuilder = Json.createArrayBuilder();
            for (URL endpoint : group.getEndpoints()) {
                endpointsBuilder.add(endpoint.toString());
            }
            groupBuilder.add("endpoints", endpointsBuilder)
                .add("group_id", group.getPaymentGroupId().toString());
            groupsBuilder.add(groupBuilder);
        }
        rootBuilder.add("groups", groupsBuilder)
            .add("assets", Json.createObjectBuilder())
            .add("service_description", Json.createObjectBuilder()
                    .add("url", "https://singnet.github.io/dnn-model-services/users_guide/i3d-video-action-recognition.html")
                    .add("description", "This service uses I3D to perform action recognition on videos."));

        return wrapExceptions(() -> { 
            String json = rootBuilder.build().toString();
            String hash = "QmR3anSdm4s13iLt3zzyrSbtvCDJNwhkrYG6yFGFHXBznb";
            when(ipfs.cat(eq(Multihash.fromBase58(hash))))
                .thenReturn(strToBytes(json));
            return new URI("ipfs://" + hash);
        });
    }

    public URI addOrganization(OrganizationMetadata metadata) {
        JsonObjectBuilder rootBuilder = Json.createObjectBuilder()
            .add("org_name", metadata.getOrgName())
            .add("org_id", metadata.getOrgId());

        JsonArrayBuilder groupsBuilder = Json.createArrayBuilder();
        for (PaymentGroup group : metadata.getPaymentGroups()) {
            PaymentDetails paymentDetails = group.getPaymentDetails();
            JsonObjectBuilder groupBuilder = Json.createObjectBuilder()
                .add("group_name", group.getGroupName())
                .add("group_id", bytesToBase64(group.getPaymentGroupId()))
                .add("payment", Json.createObjectBuilder()
                        .add("payment_address", paymentDetails.getPaymentAddress().toString())
                        .add("payment_expiration_threshold", paymentDetails.getPaymentExpirationThreshold().toString())
                        .add("payment_channel_storage_type", "etcd")
                        .add("payment_channel_storage_client", Json.createObjectBuilder()
                            .add("connection_timeout", "100s")
                            .add("request_timeout", "5s")
                            .add("endpoints", Json.createArrayBuilder()
                                .add("https://snet-etcd.singularitynet.io:2379")
                                )
                            )
                        );
            groupsBuilder.add(groupBuilder);
        }
        rootBuilder.add("groups", groupsBuilder);

        return wrapExceptions(() -> { 
            String json = rootBuilder.build().toString();
            String hash = "QmSesBRhz67FRixd3mGMNmQE5sNyZxdDgcNMEBmmhHk2X6";
            when(ipfs.cat(eq(Multihash.fromBase58(hash))))
                .thenReturn(strToBytes(json));
            return new URI("ipfs://" + hash);
        });
    }

}

