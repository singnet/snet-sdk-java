package io.singularitynet.sdk.ethereum;

import java.util.Arrays;

import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.Bip32ECKeyPair;

/**
 * Identity which private key is calculated using mnemonic and BIP32 wallet
 * generation algorithm.
 * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki">BIP32</a>
 */
public class MnemonicIdentity extends PrivateKeyIdentity {

    private static final int[] PATH_PREFIX = new int[] {
        44 | Bip32ECKeyPair.HARDENED_BIT,
        60 | Bip32ECKeyPair.HARDENED_BIT,
        0 | Bip32ECKeyPair.HARDENED_BIT,
        0
    };

    /**
     * New mnemonic from string and wallet index.
     * @param mnemonic mnemonic string.
     * @param walletIndex wallet index.
     */
    public MnemonicIdentity(String mnemonic, int walletIndex) {
        super(wallet(mnemonic, walletIndex));
    }

    private static Bip32ECKeyPair wallet(String mnemonic, int walletIndex) {
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, "");
        Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
        int[] path = Arrays.copyOf(PATH_PREFIX, PATH_PREFIX.length + 1);
        path[path.length - 1] = walletIndex;
        return Bip32ECKeyPair.deriveKeyPair(master, path);
    }

}
