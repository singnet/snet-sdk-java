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
import io.singularitynet.sdk.daemon.FirstEndpointDaemonConnection;
import io.singularitynet.sdk.daemon.PaymentChannelStateService;
import io.singularitynet.sdk.mpe.MultiPartyEscrowContract;
import io.singularitynet.sdk.mpe.PaymentChannelProvider;
import io.singularitynet.sdk.mpe.AskDaemonFirstPaymentChannelProvider;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.client.BaseServiceClient;
import io.singularitynet.sdk.ethereum.Signer;

public class Sdk {

    private final static Logger log = LoggerFactory.getLogger(Sdk.class);

    private final Web3j web3j;
    private final IPFS ipfs;
    private final Signer signer;
    private final Registry registry;
    private final MultiPartyEscrow mpe;

    public Sdk(Configuration config) {
        this(new ConfigurationDependencyFactory(config));
    }

    public Sdk(DependencyFactory factory) {
        this.web3j = factory.getWeb3j();
        this.ipfs = factory.getIpfs();
        this.signer = factory.getSigner();
        this.registry = factory.getRegistry();
        this.mpe = factory.getMultiPartyEscrow();
    }

    public Sdk(Web3j web3j, IPFS ipfs, Signer signer, Registry registry,
            MultiPartyEscrow mpe) {
        this.web3j = web3j;
        this.ipfs = ipfs;
        this.signer = signer;
        this.registry = registry;
        this.mpe = mpe;
    }

    public ServiceClient newServiceClient(String orgId, String serviceId,
            String endpointGroupName, PaymentStrategy paymentStrategy) {
        log.info("Start service client, orgId: {}, serviceId: {}, endpointGroupName: {}, paymentStrategy: {}",
                orgId, serviceId, endpointGroupName, paymentStrategy);

        RegistryContract registryContract = new RegistryContract(registry);
        MultiPartyEscrowContract mpeContract = new MultiPartyEscrowContract(web3j, mpe);

        MetadataStorage metadataStorage = new IpfsMetadataStorage(ipfs);
        MetadataProvider metadataProvider = new RegistryMetadataProvider(
                orgId, serviceId, registryContract, metadataStorage);

        DaemonConnection connection = new FirstEndpointDaemonConnection(
                endpointGroupName, metadataProvider);
        PaymentChannelStateService stateService = new PaymentChannelStateService(
                connection, mpeContract, web3j, signer);
        PaymentChannelProvider paymentChannelProvider = new
            AskDaemonFirstPaymentChannelProvider(mpeContract, stateService);

        return new BaseServiceClient(connection, metadataProvider,
                paymentChannelProvider, paymentStrategy, signer); 
    }

    //FIXME: replace by web3j wrapper which can also cache results instead of
    //calling JSON RPC service each time
    public Web3j getWeb3j() {
        return web3j;
    }

    public void shutdown() {
        web3j.shutdown();
        log.info("SDK shutdown");
    }

}
