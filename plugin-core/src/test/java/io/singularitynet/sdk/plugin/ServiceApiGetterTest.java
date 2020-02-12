package io.singularitynet.sdk.plugin;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.tuples.generated.Tuple4;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.contracts.Registry;

public class ServiceApiGetterTest {

    private final static String ORG_ID = "example-org";
    private final static String SERVICE_ID = "example-service";
    private final static String METADATA_HASH = "QmV4HtrgRwUVHCyDBvkeXFPPCsgweWA86uCj328wZq5Y2q";
    private final static String MODEL_IPFS_HASH = "QmPKvg2rMmACYJsVQfAa4uLB5rVV7fXF3xuet95Zinxct9";

    private Registry registry;
    private IPFS ipfs;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        registry = mock(Registry.class);
        when(registry.getServiceRegistrationById(
                    eq(Utils.strToBytes32(ORG_ID)),
                    eq(Utils.strToBytes32(SERVICE_ID))))
            .thenReturn(new RemoteCall<>(
                        () -> {
                            return new Tuple4<>(true,
                                    Utils.strToBytes32(SERVICE_ID),
                                    Utils.strToBytes("ipfs://" + METADATA_HASH),
                                    Collections.emptyList());
                        }));

        ipfs = mock(IPFS.class);
        when(ipfs.cat(eq(Multihash.fromBase58(METADATA_HASH))))
            .thenReturn(readResource("/example-service-metadata.json"));
        when(ipfs.cat(eq(Multihash.fromBase58(MODEL_IPFS_HASH))))
            .thenReturn(readResource("/example-service-model.tar"));
    }

    private static byte[] readResource(String name) {
        try {
            return Files.readAllBytes(getResourcePath(name));
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    private static String readFileAsString(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    private static Path getResourcePath(String name) {
        return Paths.get(ServiceApiGetterTest.class.getResource(name).getFile());
    }

    @Test
    public void getServiceApi() throws IOException, PluginException {
        File outputDir = testFolder.newFolder("output");
        ServiceApiGetter.Parameters params = new ServiceApiGetter.DefaultParameters() {
            public String getOrgId() { return ORG_ID; }
            public String getServiceId() { return SERVICE_ID; }
            public File getOutputDir() { return outputDir; }
            public String getJavaPackage() { return "org.example.exampleservice"; }
        };
        ServiceApiGetter getter = new ServiceApiGetter(registry, ipfs, params);

        getter.run();

        assertEquals("API Protobuf file",
                readFileAsString(getResourcePath("/example_service.proto")),
                readFileAsString(outputDir.toPath().resolve("example_service.proto")));
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void throwExceptionOnWeb3jClientConnectionError() throws PluginException {
        exceptionRule.expect(PluginException.class);
        exceptionRule.expectMessage("Could not perform operation on Ethereum RPC endpoint provided: http://localhost:1");
        ServiceApiGetter.Parameters params = new ServiceApiGetter.DefaultParameters() {
            public String getOrgId() { return ORG_ID; }
            public String getServiceId() { return SERVICE_ID; }
            public File getOutputDir() { return null; }
            public String getJavaPackage() { return "org.example.exampleservice"; }
            public URL getEthereumJsonRpcEndpoint() { 
                try {
                    return new URL("http://localhost:1");
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        ServiceApiGetter getter = new ServiceApiGetter(null, null, params);

        getter.run();
    }
}
