package io.singularitynet.sdk.ethereum;

import java.math.BigInteger;
import org.web3j.protocol.Web3j;

import io.singularitynet.sdk.common.Utils;

public class Ethereum {

    private final Web3j web3j;

    public Ethereum(Web3j web3j) {
        this.web3j = web3j;
    }

    public BigInteger getEthBlockNumber() {
        return Utils.wrapExceptions(() -> web3j.ethBlockNumber().send()
                .getBlockNumber());
    }

}
