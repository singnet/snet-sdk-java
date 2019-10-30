package io.singularitynet.sdk.test;

import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.*;
import java.math.BigInteger;
import org.web3j.protocol.core.Ethereum;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.registry.*;
import io.singularitynet.sdk.mpe.*;
import io.singularitynet.sdk.daemon.*;
import io.singularitynet.sdk.ethereum.*;

public class Environment {

    private Ethereum ethereum = mock(Ethereum.class);
    private RegistryMock registry = new RegistryMock();
    private IpfsMock ipfs = new IpfsMock();
    private MultiPartyEscrowMock mpe = new MultiPartyEscrowMock();
    private DaemonMock daemon = new DaemonMock();
    private TestServer server = TestServer.start(daemon);

    private Address mpeAddress = randomAddress();

    private Environment() {
        ethereum = newEthereumMock();
        registry = new RegistryMock();
        ipfs = new IpfsMock();
        mpe = new MultiPartyEscrowMock();
        daemon = new DaemonMock();
        server = TestServer.start(daemon);

        mpeAddress = randomAddress();
        mpe.setContractAddress(mpeAddress);
    }

    public static Environment env() {
        return new Environment();
    }

    public Ethereum ethereum() {
        return ethereum;
    }

    public RegistryMock registry() {
        return registry;
    }

    public IpfsMock ipfs() {
        return ipfs;
    }

    public MultiPartyEscrowMock mpe() {
        return mpe;
    }

    public DaemonMock daemon() {
        return daemon;
    }

    public TestServer server() {
        return server;
    }

    private Ethereum newEthereumMock() {
        return Utils.wrapExceptions(() -> {
            Ethereum ethereum = mock(Ethereum.class);
            BigInteger curEthBlock = BigInteger.valueOf(53);
            EthBlockNumber ethBlockNumber = mock(EthBlockNumber.class);
            when(ethBlockNumber.getBlockNumber()).thenReturn(curEthBlock);
            Request ethBlockNumberReq = mock(Request.class);
            when(ethBlockNumberReq.send()).thenReturn(ethBlockNumber);
            when(ethereum.ethBlockNumber()).thenReturn(ethBlockNumberReq);
            return ethereum;
        });
    }

    public void updateMocks() {
        registerServices();
        registerOrganizations();
    }

    private static final int ADDRESS_LENGTH = 20;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static Address randomAddress() {
        StringBuffer address = new StringBuffer();
        address.append("0x");
        for (int i = 0; i < ADDRESS_LENGTH * 2; ++i) {
            int halfByte = (int) (Math.random() * 16);
            address.append(HEX_ARRAY[halfByte]);
        }
        return new Address(address.toString());
    }

    public static byte[] randomUint256() {
        byte[] uint256 = new byte[32];
        for (int i = 0; i < 32; ++i) {
            uint256[i] = (byte) (Math.random() * 256);
        }
        return uint256;
    }

    private Map<Address, Signer> signerByAddress = new HashMap<>();

    public Signer newSigner() {
        Signer signer = new PrivateKeyIdentity(Utils.base64ToBytes("1PeCDRD7vLjqiGoHl7A+yPuJIy8TdbNc1vxOyuPjxBM="));
        signerByAddress.put(signer.getAddress(), signer);
        return signer;
    }

    private Map<String, PaymentGroup.Builder> paymentGroupById = new HashMap<>();

    public PaymentGroup.Builder newPaymentGroup() {
        byte[] groupId = randomUint256();
        PaymentGroup.Builder paymentGroup = PaymentGroup.newBuilder()
            .setGroupName("default_group")
            .setPaymentGroupId(groupId)
            .setPaymentDetails(PaymentDetails.newBuilder()
                    .setPaymentAddress(new Address("0xfA8a01E837c30a3DA3Ea862e6dB5C6232C9b800A"))
                    .setPaymentExpirationThreshold(BigInteger.valueOf(100))
                    .build());
        paymentGroupById.put(Utils.bytesToBase64(groupId), paymentGroup);
        return paymentGroup;
    }

    private Map<String, OrganizationMetadata.Builder> organizationMetadataById = new HashMap<>();

    public OrganizationMetadata.Builder newOrganizationMetadata(String orgId) {
        OrganizationMetadata.Builder metadata = OrganizationMetadata.newBuilder()
            .setOrgName("Test Organization")
            .setOrgId(orgId)
            .addPaymentGroup(newPaymentGroup().build());
        organizationMetadataById.put(orgId, metadata);
        return metadata;
    }

    private Map<String, OrganizationRegistration.Builder> orgRegistrationById = new HashMap<>();

    public OrganizationRegistration.Builder registerOrganization(String orgId) {
        OrganizationRegistration.Builder registration = OrganizationRegistration.newBuilder()
            .setOrgId(orgId);
        orgRegistrationById.put(orgId, registration);
        return registration;
    }

    private void registerOrganizations() {
        for (Map.Entry<String, OrganizationRegistration.Builder> entry : orgRegistrationById.entrySet()) {
            String orgId = entry.getKey();
            OrganizationRegistration.Builder registration = entry.getValue();
            OrganizationMetadata.Builder org = organizationMetadataById.get(orgId);

            URI orgMetadataUri = ipfs.addOrganization(org.build());
            registration.setMetadataUri(orgMetadataUri);
            registry.addOrganizationRegistration(orgId, registration.build());
        }
    }
    
    public Pricing.Builder newPricing() {
        return Pricing.newBuilder()
            .setPriceModel(PriceModel.FIXED_PRICE)
            .setPriceInCogs(BigInteger.valueOf(11));
    }

    public EndpointGroup.Builder newEndpointGroup(String orgId) {
        OrganizationMetadata.Builder org = organizationMetadataById.get(orgId);
        byte[] paymentGroupId = org.build().getPaymentGroups().get(0).getPaymentGroupId();
        return EndpointGroup.newBuilder()
            .setGroupName("default_group")
            .addPricing(newPricing().build())
            .addEndpoint(server.getEndpoint())
            .setPaymentGroupId(paymentGroupId);
    }

    private Map<String, ServiceMetadata.Builder> serviceMetadataById = new HashMap<>();

    public ServiceMetadata.Builder newServiceMetadata(String serviceId, String orgId) {
        ServiceMetadata.Builder metadata = ServiceMetadata.newBuilder()
            .setDisplayName("Test Service Name")
            .setMpeAddress(mpeAddress)
            .addEndpointGroup(newEndpointGroup(orgId).build());
        serviceMetadataById.put(serviceId, metadata);
        return metadata;
    }

    private Map<String, ServiceRegistration.Builder> serviceRegistrationById = new HashMap<>();

    public ServiceRegistration.Builder registerService(String orgId, String serviceId) {
        ServiceRegistration.Builder serviceRegistration = ServiceRegistration.newBuilder()
            .setServiceId(serviceId);
        serviceRegistrationById.put(orgId + ":" + serviceId, serviceRegistration);
        return serviceRegistration;
    }

    private void registerServices() { for (Map.Entry<String, ServiceRegistration.Builder> entry : serviceRegistrationById.entrySet()) {
            String orgIdserviceId = entry.getKey();
            int delimiter = orgIdserviceId.indexOf(":");
            String orgId = orgIdserviceId.substring(0, delimiter);
            String serviceId = orgIdserviceId.substring(delimiter + 1);
            ServiceRegistration.Builder registration = entry.getValue();
            ServiceMetadata.Builder metadata = serviceMetadataById.get(serviceId);

            OrganizationRegistration.Builder orgReg = orgRegistrationById.get(orgId);
            orgReg.addServiceId(serviceId);

            URI serviceMetadataUri = ipfs.addService(metadata.build());
            registration.setMetadataUri(serviceMetadataUri);
            registry.addServiceRegistration(orgId, serviceId, registration.build());
        }
    }

    public PaymentChannel.Builder newPaymentChannel(byte[] groupId, Signer signer) {
        BigInteger channelId = BigInteger.valueOf((long)(Math.random() * 100));
        PaymentGroup group = paymentGroupById.get(Utils.bytesToBase64(groupId)).build();
        PaymentChannel.Builder paymentChannel = PaymentChannel.newBuilder()
            .setChannelId(channelId)
            .setMpeContractAddress(mpeAddress)
            .setNonce(BigInteger.valueOf(7))
            .setSender(new Address("0xC4f3BFE7D69461B7f363509393D44357c084404c"))
            .setSigner(signer.getAddress())
            .setRecipient(group.getPaymentDetails().getPaymentAddress())
            .setPaymentGroupId(group.getPaymentGroupId())
            .setValue(BigInteger.valueOf(41))
            .setExpiration(BigInteger.valueOf(125))
            .setSpentAmount(BigInteger.valueOf(0));
        mpe.addPaymentChannel(paymentChannel.build());
        return paymentChannel;
    }

    public EscrowPayment.Builder newEscrowPayment(PaymentChannel paymentChannel) {
        return EscrowPayment.newBuilder()
            .setPaymentChannel(paymentChannel)
            .setAmount(BigInteger.valueOf(11))
            .setSigner(signerByAddress.get(paymentChannel.getSigner()));
    }

    public PaymentChannelStateReply.Builder newPaymentChannelStateReply(EscrowPayment payment) {
        return PaymentChannelStateReply.newBuilder()
            .setCurrentNonce(payment.getChannelNonce())
            .setCurrentSignedAmount(payment.getAmount())
            .setCurrentSignature(payment.getSignature());
    }

}
