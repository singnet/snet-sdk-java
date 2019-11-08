package io.singularitynet.sdk.client;

import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;
import io.ipfs.api.IPFS;
import com.google.gson.Gson;
import java.io.InputStreamReader;
import com.google.common.base.Preconditions;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.util.Map;

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
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Signer;

public class Sdk {

    private final Web3j web3j;
    private final IPFS ipfs;
    private final ContractGasProvider gasProvider;
    private final Signer signer;

    public Sdk(Configuration config) {
        this(new ConfigurationDependencyFactory(config));
    }

    public Sdk(DependencyFactory factory) {
        this.web3j = factory.getWeb3j();
        this.ipfs = factory.getIpfs();
        this.gasProvider = factory.getContractGasProvider(web3j);
        this.signer = factory.getSigner();
    }

    public ServiceClient newServiceClient(String orgId, String serviceId,
            String endpointGroupName, PaymentStrategy paymentStrategy) {

        String networkId = Utils.wrapExceptions(() -> {
            return web3j.netVersion().send().getNetVersion();
        });
        ReadonlyTransactionManager transactionManager = new ReadonlyTransactionManager(
                // TODO: add unit test on prefix adding
                web3j, signer.getAddress().toString());

        Address registryAddress = readContractAddress(networkId, "networks/Registry.json", "Registry");
        Registry registry = Registry.load(registryAddress.toString(), web3j,
                transactionManager, gasProvider);
        RegistryContract registryContract = new RegistryContract(registry);
        MetadataStorage metadataStorage = new IpfsMetadataStorage(ipfs);
        MetadataProvider metadataProvider = new RegistryMetadataProvider(
                orgId, serviceId, registryContract, metadataStorage);

        Address mpeAddress = readContractAddress(networkId, "networks/MultiPartyEscrow.json", "MultiPartyEscrow");
        MultiPartyEscrow mpe = MultiPartyEscrow.load(mpeAddress.toString(), web3j,
                transactionManager, gasProvider);
        MultiPartyEscrowContract mpeContract = new MultiPartyEscrowContract(mpe);

        DaemonConnection connection = new FirstEndpointDaemonConnection(
                endpointGroupName, metadataProvider);
        PaymentChannelStateService stateService = new PaymentChannelStateService(
                connection, mpeContract, web3j, signer);
        PaymentChannelProvider paymentChannelProvider =
            new AskDaemonFirstPaymentChannelProvider(mpeContract, stateService);

        return new BaseServiceClient(connection, metadataProvider,
                paymentChannelProvider, paymentStrategy, signer); 
    }

    public void shutdown() {
        web3j.shutdown();
    }

    private static Address readContractAddress(String networkId, String networkJson, String contractName) {
        return Utils.wrapExceptions(() -> {
            InputStreamReader jsonReader = new InputStreamReader(Sdk.class.getClassLoader().getResourceAsStream(networkJson));
            try {
                Gson gson = new Gson();
                Type jsonType = new TypeToken<Map<String, Map<String, Object>>>(){}.getType();
                Map<String, Map<String, Object>> networks = gson.fromJson(jsonReader, jsonType);
                Map<String, Object> network = networks.get(networkId);
                // TODO: test precondition
                Preconditions.checkState(network != null, "No configuration for network %s found", networkId);
                Object address = network.get("address");
                // TODO: test precondition
                Preconditions.checkState(address != null, "No address of %s contract found in the network %s configuration",
                        contractName, networkId);
                return new Address((String) address);
            } finally {
                jsonReader.close();
            }
        });
    }

}
