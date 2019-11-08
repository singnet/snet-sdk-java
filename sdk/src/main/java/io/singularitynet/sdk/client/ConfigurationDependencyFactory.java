package io.singularitynet.sdk.client;

import java.net.URL;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import io.ipfs.api.IPFS;

import io.singularitynet.sdk.ethereum.Signer;
import io.singularitynet.sdk.ethereum.MnemonicIdentity;
import io.singularitynet.sdk.ethereum.PrivateKeyIdentity;

public class ConfigurationDependencyFactory implements DependencyFactory {

    private final Configuration config;

    public ConfigurationDependencyFactory(Configuration config) {
        this.config = config;
    }

    @Override
    public Web3j getWeb3j() {
        return Web3j.build(new HttpService(config.getEthereumJsonRpcEndpoint()));
    }

    @Override
    public ContractGasProvider getContractGasProvider(Web3j web3k) {
        return new DefaultGasProvider();
    }

    @Override
    public IPFS getIpfs() {
        URL ipfsUrl = config.getIpfsUrl();
        // TODO: support https
        return new IPFS(ipfsUrl.getHost(), ipfsUrl.getPort());
    }

    @Override
    public Signer getSigner() {
        switch (config.getSignerType()) {
            case MNEMONIC:
                return new MnemonicIdentity(config.getSignerMnemonic(), 0);
            case PRIVATE_KEY:
                return new PrivateKeyIdentity(config.getSignerPrivateKey());
            default:
                throw new IllegalArgumentException("Unexpected signer type: " + config.getSignerType());
        }
    }

}
