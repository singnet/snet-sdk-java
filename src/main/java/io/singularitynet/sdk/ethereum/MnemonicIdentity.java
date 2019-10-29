package io.singularitynet.sdk.ethereum;

import org.web3j.crypto.WalletUtils;

public class MnemonicIdentity extends PrivateKeyIdentity {

    public MnemonicIdentity(String mnemonic) {
        // TODO: test that private key generated is equal to snet cli one
        super(WalletUtils.loadBip39Credentials("", mnemonic).getEcKeyPair());
    }

}
