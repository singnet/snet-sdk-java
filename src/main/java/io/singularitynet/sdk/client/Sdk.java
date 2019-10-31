package io.singularitynet.sdk.client;

import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.protocol.Web3j;
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

public class Sdk {

    private final Configuration config;
    private final Web3j web3j;

    public Sdk(Configuration config) {
        this.config = config;
        this.web3j = config.getWeb3j();
    }

    public ServiceClient newServiceClient(String orgId, String serviceId,
            String endpointGroupName, PaymentStrategy paymentStrategy) {

        String networkId = Utils.wrapExceptions(() -> {
            return web3j.netVersion().send().getNetVersion();
        });
        ReadonlyTransactionManager transactionManager = new ReadonlyTransactionManager(
                // TODO: add unit test on prefix adding
                web3j, config.getSigner().getAddress().toString());

        Address registryAddress = readContractAddress(networkId, "networks/Registry.json", "Registry");
        Registry registry = Registry.load(registryAddress.toString(), web3j,
                transactionManager, config.getContractGasProvider());
        RegistryContract registryContract = new RegistryContract(registry);
        MetadataStorage metadataStorage = new IpfsMetadataStorage(config.getIpfs());
        MetadataProvider metadataProvider = new RegistryMetadataProvider(
                orgId, serviceId, registryContract, metadataStorage);

        Address mpeAddress = readContractAddress(networkId, "networks/MultiPartyEscrow.json", "MultiPartyEscrow");
        MultiPartyEscrow mpe = MultiPartyEscrow.load(mpeAddress.toString(), web3j,
                transactionManager, config.getContractGasProvider());
        MultiPartyEscrowContract mpeContract = new MultiPartyEscrowContract(mpe);

        DaemonConnection connection = new FirstEndpointDaemonConnection(
                endpointGroupName, metadataProvider);
        PaymentChannelStateService stateService = new PaymentChannelStateService(
                connection, web3j, config.getSigner());
        PaymentChannelProvider paymentChannelProvider =
            new AskDaemonFirstPaymentChannelProvider(mpeContract, stateService);

        return new BaseServiceClient(connection, metadataProvider,
                paymentChannelProvider, paymentStrategy); 
    }

    public void shutdown() {
        // FIXME: select the class which owns web3j
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
