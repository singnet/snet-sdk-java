package io.singularitynet.sdk.client;

import java.net.URL;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.ipfs.api.IPFS;

import io.singularitynet.sdk.common.Preconditions;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.ContractUtils;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.MnemonicIdentity;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;
import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;

public class ConfigurationDependencyFactory implements DependencyFactory {

    private final static Logger log = LoggerFactory.getLogger(ConfigurationDependencyFactory.class);

    private final Web3j web3j;
    private final IPFS ipfs;
    private final Identity identity;
    private final Registry registry;
    private final MultiPartyEscrow mpe;

    public ConfigurationDependencyFactory(Configuration config) {
        Preconditions.checkArgument(config.getEthereumJsonRpcEndpoint() != null,
                "Ethereum JSON RPC endpoint is required");
        Preconditions.checkArgument(config.getIpfsEndpoint() != null,
                "IPFS endpoint is required");
        Preconditions.checkArgument(config.getIdentityType() != null,
                "Identity type is required");

        log.info("Construct SDK dependencies");

        log.info("Open connection to Ethereum RPC endpoint, ethereumJsonRpcEndpoint: {}", config.getEthereumJsonRpcEndpoint());
        this.web3j = Web3j.build(new HttpService(config.getEthereumJsonRpcEndpoint().toString()));

        URL ipfsEndpoint = config.getIpfsEndpoint();
        log.info("Open connection to IPFS RPC endpoint, ipfsEndpoint: {}", ipfsEndpoint);
        this.ipfs = new IPFS(ipfsEndpoint.getHost(), ipfsEndpoint.getPort());

        StaticGasProvider gasProvider = new StaticGasProvider(
                config.getGasPrice().orElse(DefaultGasProvider.GAS_PRICE),
                config.getGasLimit().orElse(DefaultGasProvider.GAS_LIMIT)
                );
        TransactionManager transactionManager;

        log.info("New identity, type: {}", config.getIdentityType());
        switch (config.getIdentityType()) {
            case MNEMONIC:
                {
                    Preconditions.checkArgument(config.getIdentityMnemonic().isPresent(), "No identity mnemonic specified");
                    PrivateKeyIdentity identity = new MnemonicIdentity(config.getIdentityMnemonic().get(), 0);
                    transactionManager = new RawTransactionManager(web3j, identity.getCredentials());
                    this.identity = identity;
                }
                break;
            case PRIVATE_KEY:
                {
                    Preconditions.checkArgument(config.getIdentityPrivateKey().isPresent(), "No identity private key specified");
                    PrivateKeyIdentity identity = new PrivateKeyIdentity(config.getIdentityPrivateKey().get());
                    transactionManager = new RawTransactionManager(web3j, identity.getCredentials());
                    this.identity = identity;
                }
                break;
            default:
                throw new IllegalArgumentException("Unexpected identity type: " + config.getIdentityType());
        }

        String networkId = Utils.wrapExceptions(() -> {
            return web3j.netVersion().send().getNetVersion();
        });
        log.info("Ethereum network id, networkId: {}", networkId);

        Address registryAddress;
        if (config.getRegistryAddress().isPresent()) {
            registryAddress = config.getRegistryAddress().get();
        } else {
            log.info("No Registry address in config, using default Registry address");
            registryAddress = ContractUtils.readContractAddress(networkId, "Registry");
        }
        log.info("Registry address, registryAddress: {}", registryAddress);
        this.registry = Registry.load(registryAddress.toString(), web3j,
                transactionManager, gasProvider);

        Address mpeAddress;
        if (config.getMultiPartyEscrowAddress().isPresent()) {
            mpeAddress = config.getMultiPartyEscrowAddress().get();
        } else {
            log.info("No MultiPartyEscrow address in config, using default MultiPartyEscrow address");
            mpeAddress = ContractUtils.readContractAddress(networkId, "MultiPartyEscrow");
        }
        log.info("MultiPartyEscrow address, mpeAddress: {}", mpeAddress);
        this.mpe = MultiPartyEscrow.load(mpeAddress.toString(), web3j,
                transactionManager, gasProvider);
    }

    @Override
    public Web3j getWeb3j() {
        return web3j;
    }

    @Override
    public IPFS getIpfs() {
        return ipfs;
    }

    @Override
    public Identity getIdentity() {
        return identity;
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    @Override
    public MultiPartyEscrow getMultiPartyEscrow() {
        return mpe;
    }

}
