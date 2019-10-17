package io.singularitynet.sdk.client;

import io.singularitynet.sdk.contracts.Registry;
import io.ipfs.api.IPFS;

public interface Config {
    
    Registry getRegistry();
    IPFS getIpfs();

}
