package io.singularitynet.sdk.ethereum;

import java.math.BigInteger;
import org.web3j.protocol.Web3j;

import io.singularitynet.sdk.common.Utils;

/**
 * Web3j wrapper to provide single point of access to the Ethereum JSON RPC.
 */
public class Ethereum {

    private final Web3j web3j;

    /**
     * Constructor.
     * @param web3j web3j instance.
     */
    public Ethereum(Web3j web3j) {
        this.web3j = web3j;
    }

    /**
     * Return latest Ethereum block number.
     * @return ethereum block number.
     */
    public BigInteger getEthBlockNumber() {
        return Utils.wrapExceptions(() -> web3j.ethBlockNumber().send()
                .getBlockNumber());
    }

}
