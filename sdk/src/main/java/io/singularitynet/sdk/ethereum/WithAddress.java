package io.singularitynet.sdk.ethereum;

/**
 * Entity which has an Ethereum address.
 */
public interface WithAddress {

    /**
     * Return Ethereum address of the entity.
     * @return Ethereum address.
     */
    Address getAddress();

}
