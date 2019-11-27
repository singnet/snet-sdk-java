package io.singularitynet.sdk.client;

import org.web3j.protocol.Web3j;
import io.ipfs.api.IPFS;

import io.singularitynet.sdk.ethereum.Signer;
import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;

public interface DependencyFactory {

    Web3j getWeb3j();
    IPFS getIpfs();
    Signer getSigner();
    Registry getRegistry();
    MultiPartyEscrow getMultiPartyEscrow();

}
