package io.singularitynet.sdk.client;

import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;
import io.ipfs.api.IPFS;

import io.singularitynet.sdk.ethereum.Signer;

public interface Configuration {

    Web3j getWeb3j();
    ContractGasProvider getContractGasProvider();
    IPFS getIpfs();
    Signer getSigner();

}
