package io.singularitynet.sdk.client;

import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;
import io.ipfs.api.IPFS;

import io.singularitynet.sdk.ethereum.Signer;

public interface DependencyFactory {

    Web3j getWeb3j();
    ContractGasProvider getContractGasProvider(Web3j web3j);
    IPFS getIpfs();
    Signer getSigner();

}
