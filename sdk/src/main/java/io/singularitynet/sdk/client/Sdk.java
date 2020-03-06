package io.singularitynet.sdk.client;

import io.ipfs.api.IPFS;
import java.math.BigInteger;
import org.web3j.protocol.Web3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.registry.RegistryContract;
import io.singularitynet.sdk.registry.MetadataStorage;
import io.singularitynet.sdk.registry.IpfsMetadataStorage;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.CachingMetadataProvider;
import io.singularitynet.sdk.registry.RegistryMetadataProvider;
import io.singularitynet.sdk.daemon.DaemonConnection;
import io.singularitynet.sdk.daemon.RandomEndpointDaemonConnection;
import io.singularitynet.sdk.daemon.PaymentChannelStateService;
import io.singularitynet.sdk.daemon.FreeCallStateService;
import io.singularitynet.sdk.mpe.AskDaemonFirstPaymentChannelProvider;
import io.singularitynet.sdk.mpe.MultiPartyEscrowContract;
import io.singularitynet.sdk.mpe.PaymentChannelStateProvider;
import io.singularitynet.sdk.mpe.BlockchainPaymentChannelManager;
import io.singularitynet.sdk.mpe.MpePaymentChannelManager;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.client.BaseServiceClient;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.ethereum.Address;

/**
 * SDK class bootstraps main SDK components according to the given
 * configuration. It is used to create instance of the ServiceClient for
 * specific service. To cleanup resources (web3j connection specifically)
 * Sdk.shutdown() method should be called.
 * @see io.singularitynet.sdk.client.ServiceClient
 */
public class Sdk {

    private final static Logger log = LoggerFactory.getLogger(Sdk.class);

    private final Web3j web3j;
    private final IPFS ipfs;
    private final Identity identity;
    private final Registry registry;
    private final MultiPartyEscrow mpe;

    private final Ethereum ethereum;
    private final MultiPartyEscrowContract mpeContract;
    private final MetadataStorage metadataStorage;
    private final RegistryContract registryContract;
    private final MpePaymentChannelManager paymentChannelManager;

    /**
     * New SDK instance for the configuration given.
     * @param config configuration instance.
     */
    public Sdk(Configuration config) {
        this(new ConfigurationDependencyFactory(config));
    }

    /**
     * New SDK instance for ready to use dependency factory.
     * @param factory dependency factory instance.
     */
    public Sdk(DependencyFactory factory) {
        this(factory.getWeb3j(), factory.getIpfs(), factory.getIdentity(),
                factory.getRegistry(), factory.getMultiPartyEscrow());
    }

    /**
     * New SDK instance with all dependencies provided manually.
     * @param web3j web3j instance.
     * @param ipfs IPFS instance.
     * @param identity identity instance.
     * @param registry Registry contract instance.
     * @param mpe MultiPartyEscrow contract instance.
     */
    public Sdk(Web3j web3j, IPFS ipfs, Identity identity, Registry registry,
            MultiPartyEscrow mpe) {
        this.web3j = web3j;
        this.ipfs = ipfs;
        this.identity = identity;
        this.registry = registry;
        this.mpe = mpe;

        this.ethereum = new Ethereum(web3j);
        this.mpeContract = new MultiPartyEscrowContract(web3j, mpe);
        this.metadataStorage = new IpfsMetadataStorage(ipfs);
        this.registryContract = new RegistryContract(registry);
        this.paymentChannelManager = new MpePaymentChannelManager(mpeContract);
    }

    // TODO: add BaseServiceClient constructor which performs all necessary
    // initializations.
    /**
     * Return new instance of the ServiceClient for the given service.
     * @param orgId organization id.
     * @param serviceId service id.
     * @param endpointGroupName name of the enpoint group to connect.
     * @param paymentStrategy payment strategy to use.
     * @return new instance of service client.
     */
    public ServiceClient newServiceClient(String orgId, String serviceId,
            String endpointGroupName, PaymentStrategy paymentStrategy) {
        log.info("Start service client, orgId: {}, serviceId: {}, endpointGroupName: {}, paymentStrategy: {}",
                orgId, serviceId, endpointGroupName, paymentStrategy);

        MetadataProvider metadataProvider = getMetadataProvider(orgId, serviceId);

        DaemonConnection connection = new RandomEndpointDaemonConnection(
                endpointGroupName, metadataProvider);

        PaymentChannelStateService stateService = new PaymentChannelStateService(
                connection, mpeContract.getContractAddress(), ethereum);
        PaymentChannelStateProvider paymentChannelStateProvider =
            new AskDaemonFirstPaymentChannelProvider(mpeContract, stateService);
        FreeCallStateService freeCallStateService = new FreeCallStateService(
                orgId, serviceId, ethereum, metadataProvider, connection);

        return new BaseServiceClient(serviceId, connection, metadataProvider,
                paymentChannelStateProvider, freeCallStateService, paymentStrategy); 
    }

    /**
     * @return Ethereum wrapper.
     */
    public Ethereum getEthereum() {
        return ethereum;
    }

    /**
     * Transfer AGI tokens to the given address.
     * @param toAddress target Ethereum address.
     * @param amount number of cogs to tranfer.
     */
    public void transfer(Address toAddress, BigInteger amount) {
        // TODO: implement functions to convert cogs and AGIs
        mpeContract.transfer(toAddress, amount);
    }

    /**
     * @return Ethereum identity.
     */
    public Identity getIdentity() {
        return identity;
    }

    /**
     * Return metadata provider for the given organiation and service id.
     * @param orgId organization id.
     * @param serviceId service id.
     * @return metadata provider instance.
     */
    public MetadataProvider getMetadataProvider(String orgId, String serviceId) {
        return new CachingMetadataProvider(
                new RegistryMetadataProvider(orgId, serviceId, registryContract,
                    metadataStorage));
    }

    /**
     * @return blockchain MultiPartyEscrow payment channel manager.
     */
    public BlockchainPaymentChannelManager getBlockchainPaymentChannelManager() {
        return paymentChannelManager;
    }

    /**
     * Shutdown SDK and release all resources aquired.
     */
    public void shutdown() {
        web3j.shutdown();
        log.info("SDK shutdown");
    }

}
