package io.singularitynet.sdk.client;

import org.web3j.protocol.Web3j;
import io.ipfs.api.IPFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.registry.RegistryContract;
import io.singularitynet.sdk.registry.MetadataStorage;
import io.singularitynet.sdk.registry.IpfsMetadataStorage;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.RegistryMetadataProvider;
import io.singularitynet.sdk.daemon.DaemonConnection;
import io.singularitynet.sdk.daemon.RandomEndpointDaemonConnection;
import io.singularitynet.sdk.daemon.PaymentChannelStateService;
import io.singularitynet.sdk.mpe.MultiPartyEscrowContract;
import io.singularitynet.sdk.mpe.PaymentChannelStateProvider;
import io.singularitynet.sdk.mpe.PaymentChannelManager;
import io.singularitynet.sdk.mpe.MpePaymentChannelManager;
import io.singularitynet.sdk.mpe.AskDaemonFirstPaymentChannelProvider;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.client.BaseServiceClient;
import io.singularitynet.sdk.ethereum.Identity;

public class Sdk {

    private final static Logger log = LoggerFactory.getLogger(Sdk.class);

    private final Web3j web3j;
    private final IPFS ipfs;
    private final Identity identity;
    private final Registry registry;
    private final MultiPartyEscrow mpe;

    private final MultiPartyEscrowContract mpeContract;
    private final MetadataStorage metadataStorage;
    private final RegistryContract registryContract;
    private final MpePaymentChannelManager paymentChannelManager;

    public Sdk(Configuration config) {
        this(new ConfigurationDependencyFactory(config));
    }

    public Sdk(DependencyFactory factory) {
        this(factory.getWeb3j(), factory.getIpfs(), factory.getIdentity(),
                factory.getRegistry(), factory.getMultiPartyEscrow());
    }

    public Sdk(Web3j web3j, IPFS ipfs, Identity identity, Registry registry,
            MultiPartyEscrow mpe) {
        this.web3j = web3j;
        this.ipfs = ipfs;
        this.identity = identity;
        this.registry = registry;
        this.mpe = mpe;

        this.mpeContract = new MultiPartyEscrowContract(web3j, mpe);
        this.metadataStorage = new IpfsMetadataStorage(ipfs);
        this.registryContract = new RegistryContract(registry);
        this.paymentChannelManager = new MpePaymentChannelManager(mpeContract);
    }

    // TODO: move BaseServiceClient related initialization into
    // BaseServiceClient constructor.
    public ServiceClient newServiceClient(String orgId, String serviceId,
            String endpointGroupName, PaymentStrategy paymentStrategy) {
        log.info("Start service client, orgId: {}, serviceId: {}, endpointGroupName: {}, paymentStrategy: {}",
                orgId, serviceId, endpointGroupName, paymentStrategy);

        MetadataProvider metadataProvider = getMetadataProvider(orgId, serviceId);

        DaemonConnection connection = new RandomEndpointDaemonConnection(
                endpointGroupName, metadataProvider);

        PaymentChannelStateService stateService = new PaymentChannelStateService(
                connection, mpeContract, web3j, identity);
        PaymentChannelStateProvider paymentChannelStateProvider =
            new AskDaemonFirstPaymentChannelProvider(mpeContract, stateService);

        return new BaseServiceClient(connection, metadataProvider,
                paymentChannelStateProvider, paymentStrategy); 
    }

    //FIXME: replace by web3j wrapper which can also cache results instead of
    //calling JSON RPC service each time
    public Web3j getWeb3j() {
        return web3j;
    }

    public Identity getIdentity() {
        return identity;
    }

    public MetadataProvider getMetadataProvider(String orgId, String serviceId) {
        return new RegistryMetadataProvider(orgId, serviceId, registryContract,
                metadataStorage);
    }

    public PaymentChannelManager getPaymentChannelManager() {
        return paymentChannelManager;
    }

    public void shutdown() {
        web3j.shutdown();
        log.info("SDK shutdown");
    }

}
