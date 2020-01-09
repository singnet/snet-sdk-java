package io.singularitynet.sdk.ethereum;

import static org.junit.Assert.*;
import org.junit.*;

import io.singularitynet.sdk.common.Utils;

public class MnemonicIndentityTest {

    private static String mnemonic = "insect lottery stable theme shrimp expose match frog entry always viable cabbage mechanic cinnamon spread";
    private static byte[] message = Utils.strToBytes32("Some message");

    @Test
    public void generateMnemonicIdentityWallet0() {
        PrivateKeyIdentity expected = fromBase64Key("pAIRj43MIe6/Yj6AigjI0X8jYzDSk929nUSkRBRJcVA=");

        MnemonicIdentity actual = new MnemonicIdentity(mnemonic, 0);

        assertEquals("Signature", expected.sign(message), actual.sign(message));
    }

    @Test
    public void generateMnemonicIdentityWallet42() {
        PrivateKeyIdentity expected = fromBase64Key("gczjSyFyWK024XVHDipAsFr3EP3v3NtDanNsrO4O1D8=");

        MnemonicIdentity actual = new MnemonicIdentity(mnemonic, 42);

        assertEquals("Signature", expected.sign(message), actual.sign(message));
    }

    private static PrivateKeyIdentity fromBase64Key(String key) {
        byte[] privateKey = Utils.base64ToBytes(key);
        return new PrivateKeyIdentity(privateKey);
    }

}
