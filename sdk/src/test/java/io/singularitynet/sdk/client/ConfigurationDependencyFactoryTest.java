package io.singularitynet.sdk.client;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;

import java.net.URL;
import java.net.MalformedURLException;

public class ConfigurationDependencyFactoryTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void throwExceptionOnClientConnectionError() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Could not perform operation on Ethereum RPC endpoint provided: http://localhost:1");
        Configuration config = StaticConfiguration.newBuilder()
            .setEthereumJsonRpcEndpoint("http://localhost:1")
            .setIpfsEndpoint("http://ipfs.singularitynet.io:80")
            .setIdentityType(Configuration.IdentityType.MNEMONIC)
            .build();

        new ConfigurationDependencyFactory(config);
    }
}
