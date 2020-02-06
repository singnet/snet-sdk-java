package io.singularitynet.sdk.client;

import org.web3j.protocol.Web3j;
import io.ipfs.api.IPFS;

import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;

public interface DependencyFactory {

    Web3j getWeb3j();
    IPFS getIpfs();
    Identity getIdentity();
    Registry getRegistry();
    MultiPartyEscrow getMultiPartyEscrow();

}
