package io.singularitynet.sdk.ethereum;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;
import io.singularitynet.sdk.common.Preconditions;
import com.google.gson.reflect.TypeToken;

import io.singularitynet.sdk.common.Utils;

public class ContractUtils {

    private static final Map<String, String> NETWORK_CONFIG_BY_CONTRACT_NAME = new HashMap<String, String>() {{
        put("Registry", "networks/Registry.json");
        put("MultiPartyEscrow", "networks/MultiPartyEscrow.json");
    }};

    private ContractUtils() {
    }

    public static Address readContractAddress(String networkId, String contractName) {
        return Utils.wrapExceptions(() -> {
            String networkJson = NETWORK_CONFIG_BY_CONTRACT_NAME.get(contractName);
            // TODO: test precondition
            Preconditions.checkArgument(networkJson != null, "No configuration for contract %s found", contractName);
            InputStreamReader jsonReader = new InputStreamReader(ContractUtils.class.getClassLoader().getResourceAsStream(networkJson));
            try {
                Gson gson = new Gson();
                Type jsonType = new TypeToken<Map<String, Map<String, Object>>>(){}.getType();
                Map<String, Map<String, Object>> networks = gson.fromJson(jsonReader, jsonType);
                Map<String, Object> network = networks.get(networkId);
                // TODO: test precondition
                Preconditions.checkState(network != null, "No configuration for network %s found", networkId);
                Object address = network.get("address");
                // TODO: test precondition
                Preconditions.checkState(address != null, "No contract address was found in %s contract configuration for network id %s",
                        contractName, networkId);
                return new Address((String) address);
            } finally {
                jsonReader.close();
            }
        });
    }
}
