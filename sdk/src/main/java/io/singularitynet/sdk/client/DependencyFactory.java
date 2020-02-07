package io.singularitynet.sdk.client;

import org.web3j.protocol.Web3j;
import io.ipfs.api.IPFS;

import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;

/**
 * Bootstraps SDK dependencies.
 */
public interface DependencyFactory {

    /**
     * @return web3j instance.
     */
    Web3j getWeb3j();

    /**
     * @return IPFS instance.
     */
    IPFS getIpfs();

    /**
     * @return Ethereum identity.
     */
    Identity getIdentity();

    /**
     * @return Registry contract instance.
     */
    Registry getRegistry();

    /**
     * @return MultiPartyEscrow contract instance.
     */
    MultiPartyEscrow getMultiPartyEscrow();

}
