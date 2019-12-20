package io.singularitynet.sdk.client;

import java.net.URL;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.ReadonlyTransactionManager;
import io.ipfs.api.IPFS;
import io.singularitynet.sdk.common.Preconditions;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.ContractUtils;
import io.singularitynet.sdk.ethereum.Signer;
import io.singularitynet.sdk.ethereum.MnemonicIdentity;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;
import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;

public class ConfigurationDependencyFactory implements DependencyFactory {

    private final Web3j web3j;
    private final IPFS ipfs;
    private final Signer signer;
    private final Registry registry;
    private final MultiPartyEscrow mpe;

    public ConfigurationDependencyFactory(Configuration config) {
        Preconditions.checkArgument(config.getEthereumJsonRpcEndpoint() != null,
                "Ethereum JSON RPC endpoint is required");
        Preconditions.checkArgument(config.getIpfsUrl() != null,
                "IPFS endpoint is required");
        Preconditions.checkArgument(config.getSignerType() != null,
                "Signer type is required");

        this.web3j = Web3j.build(new HttpService(config.getEthereumJsonRpcEndpoint()));

        URL ipfsUrl = config.getIpfsUrl();
        this.ipfs = new IPFS(ipfsUrl.getHost(), ipfsUrl.getPort());

        switch (config.getSignerType()) {
            case MNEMONIC:
                this.signer = new MnemonicIdentity(config.getSignerMnemonic(), 0);
                break;
            case PRIVATE_KEY:
                this.signer = new PrivateKeyIdentity(config.getSignerPrivateKey());
                break;
            default:
                throw new IllegalArgumentException("Unexpected signer type: " + config.getSignerType());
        }

        String networkId = Utils.wrapExceptions(() -> {
            return web3j.netVersion().send().getNetVersion();
        });
        DefaultGasProvider gasProvider = new DefaultGasProvider();
        ReadonlyTransactionManager transactionManager = new ReadonlyTransactionManager(
                // TODO: add unit test on prefix adding
                web3j, signer.getAddress().toString());

        Address registryAddress = config.getRegistryAddress();
        if (registryAddress == null) {
            registryAddress = ContractUtils.readContractAddress(networkId, "Registry");
        }
        this.registry = Registry.load(registryAddress.toString(), web3j,
                transactionManager, gasProvider);

        Address mpeAddress = config.getMultiPartyEscrowAddress();
        if (mpeAddress == null) {
            mpeAddress = ContractUtils.readContractAddress(networkId, "MultiPartyEscrow");
        }
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
    public Signer getSigner() {
        return signer;
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
