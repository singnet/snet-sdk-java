package io.singularitynet.sdk.registry;

import org.junit.*;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;

public class RegistryMetadataProviderTest {

    private static final String SERVICE_METADATA_JSON_ALL_FIELDS = 
        "{\n" +
        "    \"version\": 1,\n" +
        "    \"display_name\": \"Example Service\",\n" +
        "    \"encoding\": \"proto\",\n" +
        "    \"service_type\": \"grpc\",\n" +
        "    \"model_ipfs_hash\": \"QmRmYMW3DLLpdux2CrE86dWobpCGYCDtYXqr9cbjMwLL2g\",\n" +
        "    \"mpe_address\": \"0x5C7a4290F6F8FF64c69eEffDFAFc8644A4Ec3a4E\",\n" +
        "    \"groups\": [\n" +
        "        {\n" +
        "            \"group_name\": \"default_group\",\n" +
        "            \"endpoints\": [\n" +
        "                \"http://localhost:7000\"\n" +
        "            ],\n" +
        "            \"pricing\": [\n" +
        "                {\n" +
        "                    \"price_model\": \"fixed_price\",\n" +
        "                    \"price_in_cogs\": 1,\n" +
        "                    \"default\": true\n" +
        "                }\n" +
        "            ],\n" +
        "            \"free_calls\": 2,\n" +
        "            \"free_call_signer_address\": \"0x592E3C0f3B038A0D673F19a18a773F993d4b2610\",\n" +
        "            \"group_id\": \"7G8/7SPnr5qLDdHOdqsRolu8wx1eQaAmzGwsnYUUI8c=\"\n" +
        "        }\n" +
        "    ],\n" +
        "    \"assets\": {},\n" +
        "    \"service_description\": {\n" +
        "        \"description\": \"Example service description\",\n" +
        "        \"url\": \"https://example.service.users.guide\"\n" +
        "    }\n" +
        "}";

    private static final ServiceMetadata SERVICE_METADATA_OBJECT_ALL_FIELDS =
        ServiceMetadata.newBuilder()
        .setDisplayName("Example Service")
        .setModelIpfsHash("QmRmYMW3DLLpdux2CrE86dWobpCGYCDtYXqr9cbjMwLL2g")
        .setMpeAddress(new Address("0x5C7a4290F6F8FF64c69eEffDFAFc8644A4Ec3a4E"))
        .addEndpointGroup(EndpointGroup.newBuilder()
                .setGroupName("default_group")
                .addPricing(Pricing.newBuilder()
                    .setPriceModel(PriceModel.FIXED_PRICE)
                    .setPriceInCogs(BigInteger.valueOf(1))
                    .build())
                .addEndpoint(Utils.strToUrl("http://localhost:7000"))
                .setPaymentGroupId(new PaymentGroupId("7G8/7SPnr5qLDdHOdqsRolu8wx1eQaAmzGwsnYUUI8c="))
                .setFreeCalls(2)
                .setFreeCallSignerAddress(new Address("0x592E3C0f3B038A0D673F19a18a773F993d4b2610"))
                .build())
        .build();

    private static final String SERVICE_METADATA_JSON_NO_OPTIONAL_FIELDS = 
        "{\n" +
        "    \"version\": 1,\n" +
        "    \"display_name\": \"Example Service\",\n" +
        "    \"encoding\": \"proto\",\n" +
        "    \"service_type\": \"grpc\",\n" +
        "    \"model_ipfs_hash\": \"QmRmYMW3DLLpdux2CrE86dWobpCGYCDtYXqr9cbjMwLL2g\",\n" +
        "    \"mpe_address\": \"0x5C7a4290F6F8FF64c69eEffDFAFc8644A4Ec3a4E\",\n" +
        "    \"groups\": [\n" +
        "        {\n" +
        "            \"group_name\": \"default_group\",\n" +
        "            \"endpoints\": [\n" +
        "                \"http://localhost:7000\"\n" +
        "            ],\n" +
        "            \"pricing\": [\n" +
        "                {\n" +
        "                    \"price_model\": \"fixed_price\",\n" +
        "                    \"price_in_cogs\": 1,\n" +
        "                    \"default\": true\n" +
        "                }\n" +
        "            ],\n" +
        "            \"group_id\": \"7G8/7SPnr5qLDdHOdqsRolu8wx1eQaAmzGwsnYUUI8c=\"\n" +
        "        }\n" +
        "    ]\n" +
        "}";

    private static final ServiceMetadata SERVICE_METADATA_OBJECT_NO_OPTIONAL_FIELDS =
        ServiceMetadata.newBuilder()
        .setDisplayName("Example Service")
        .setModelIpfsHash("QmRmYMW3DLLpdux2CrE86dWobpCGYCDtYXqr9cbjMwLL2g")
        .setMpeAddress(new Address("0x5C7a4290F6F8FF64c69eEffDFAFc8644A4Ec3a4E"))
        .addEndpointGroup(EndpointGroup.newBuilder()
                .setGroupName("default_group")
                .addPricing(Pricing.newBuilder()
                    .setPriceModel(PriceModel.FIXED_PRICE)
                    .setPriceInCogs(BigInteger.valueOf(1))
                    .build())
                .addEndpoint(Utils.strToUrl("http://localhost:7000"))
                .setPaymentGroupId(new PaymentGroupId("7G8/7SPnr5qLDdHOdqsRolu8wx1eQaAmzGwsnYUUI8c="))
                .build())
        .build();

    private static final String ORGANIZATION_METADATA_JSON =
        "{" +
        "    \"org_name\": \"snet\"," +
        "    \"org_id\": \"snet\"," +
        "    \"org_type\": \"\"," +
        "    \"description\": {}," +
        "    \"assets\": {}," +
        "    \"contacts\": []," +
        "    \"groups\": [" +
        "        {" +
        "            \"group_name\": \"default_group\"," +
        "            \"group_id\": \"EoFmN3nvaXpf6ew8jJbIPVghE5NXfYupFF7PkRmVyGQ=\"," +
        "            \"payment\": {" +
        "                \"payment_address\": \"0xd1C9246f6A15A86bae293a3E72F28C57Da6e1dCD\"," +
        "                \"payment_expiration_threshold\": 100," +
        "                \"payment_channel_storage_type\": \"etcd\"," +
        "                \"payment_channel_storage_client\": {" +
        "                    \"connection_timeout\": \"100s\"," +
        "                    \"request_timeout\": \"5s\"," +
        "                    \"endpoints\": [" +
        "                        \"https://snet-etcd.singularitynet.io:2379\"" +
        "                    ]" +
        "                }" +
        "            }" +
        "        }" +
        "    ]" +
        "}";

    private static final OrganizationMetadata ORGANIZATION_METADATA_OBJECT =
        OrganizationMetadata.newBuilder()
        .setOrgId("snet")
        .setOrgName("snet")
        .addPaymentGroup(PaymentGroup.newBuilder()
                .setGroupName("default_group")
                .setPaymentGroupId(new PaymentGroupId("EoFmN3nvaXpf6ew8jJbIPVghE5NXfYupFF7PkRmVyGQ="))
                .setPaymentDetails(PaymentDetails.newBuilder()
                    .setPaymentAddress(new Address("0xd1C9246f6A15A86bae293a3E72F28C57Da6e1dCD"))
                    .setPaymentExpirationThreshold(BigInteger.valueOf(100))
                    .build())
                .build())
        .build();

    private RegistryContract registry;
    private MetadataStorage storage;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        this.registry = mock(RegistryContract.class);
        this.storage = mock(MetadataStorage.class);
    }

    @Test
    public void getServiceMetadataAllFields() {
        addServiceMetadataToRegistry("test-org", "test-service",
                SERVICE_METADATA_JSON_ALL_FIELDS);
        RegistryMetadataProvider provider = new RegistryMetadataProvider(
                "test-org", "test-service", registry, storage);

        ServiceMetadata metadata = provider.getServiceMetadata();

        assertEquals("Service metadata",
                SERVICE_METADATA_OBJECT_ALL_FIELDS, metadata);
    }

    @Test
    public void getServiceMetadataNoOptionalFields() {
        addServiceMetadataToRegistry("test-org", "test-service",
                SERVICE_METADATA_JSON_NO_OPTIONAL_FIELDS);
        RegistryMetadataProvider provider = new RegistryMetadataProvider(
                "test-org", "test-service", registry, storage);

        ServiceMetadata metadata = provider.getServiceMetadata();

        assertEquals("Service metadata",
                SERVICE_METADATA_OBJECT_NO_OPTIONAL_FIELDS,
                metadata);
    }

    @Test
    public void getServiceMetadataNoService() {
        thrown.expect(RegistryMetadataProvider.NotFoundException.class);
        thrown.expectMessage("Service not found, orgId: test-org, serviceId: test-service");
        when(registry.getServiceRegistrationById("test-org", "test-service"))
            .thenReturn(Optional.empty());
        RegistryMetadataProvider provider = new RegistryMetadataProvider(
                "test-org", "test-service", registry, storage);

        provider.getServiceMetadata();
    }

    @Test
    public void getOrganizationMetadata() {
        addOrganizationMetadataToRegistry("test-org", ORGANIZATION_METADATA_JSON);
        RegistryMetadataProvider provider = new RegistryMetadataProvider(
                "test-org", "test-service", registry, storage);

        OrganizationMetadata metadata = provider.getOrganizationMetadata();

        assertEquals("Organization metadata",
                ORGANIZATION_METADATA_OBJECT, metadata);
    }

    @Test
    public void getOrganizationMetadataNoService() {
        thrown.expect(RegistryMetadataProvider.NotFoundException.class);
        thrown.expectMessage("Organization not found, orgId: test-org");
        when(registry.getOrganizationById("test-org"))
            .thenReturn(Optional.empty());
        RegistryMetadataProvider provider = new RegistryMetadataProvider(
                "test-org", "test-service", registry, storage);

        provider.getOrganizationMetadata();
    }

    private void addServiceMetadataToRegistry(String orgId, String serviceId, String metadataJson) {
        final URI metadataURI = Utils.strToUri("ipfs://QmNMahizv1b1VuZzWyo4x6qTUyPc5AuQhhfwsV2RNuoTBq");
        when(registry.getServiceRegistrationById(orgId, serviceId))
            .thenReturn(Optional.of(ServiceRegistration.newBuilder()
                        .setServiceId(serviceId)
                        .setMetadataUri(metadataURI)
                        .build()));
        when(storage.get(metadataURI))
            .thenReturn(Utils.strToBytes(metadataJson));
    }

    private void addOrganizationMetadataToRegistry(String orgId, String metadataJson) {
        final URI metadataURI = Utils.strToUri("ipfs://QmWHm43Neb24LVprzduAhnvyTHfR1do8H9WJctiYXiToS7");
        when(registry.getOrganizationById(orgId))
            .thenReturn(Optional.of(OrganizationRegistration.newBuilder()
                        .setOrgId(orgId)
                        .setMetadataUri(metadataURI)
                        .build()));
        when(storage.get(metadataURI))
            .thenReturn(Utils.strToBytes(metadataJson));
    }

}

