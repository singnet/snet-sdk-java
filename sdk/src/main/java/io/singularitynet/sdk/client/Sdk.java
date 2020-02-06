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
import io.singularitynet.sdk.ethereum.Ethereum;
import io.singularitynet.sdk.ethereum.Address;

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

        this.ethereum = new Ethereum(web3j);
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
                connection, mpeContract.getContractAddress(), ethereum, identity);
        PaymentChannelStateProvider paymentChannelStateProvider =
            new AskDaemonFirstPaymentChannelProvider(mpeContract, stateService);

        return new BaseServiceClient(connection, metadataProvider,
                paymentChannelStateProvider, paymentStrategy); 
    }

    public Ethereum getEthereum() {
        return ethereum;
    }

    public void transfer(Address toAddress, BigInteger amount) {
        // TODO: implement functions to convert cogs and AGIs
        mpeContract.transfer(toAddress, amount);
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
