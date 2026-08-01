package io.singularitynet.sdk.maven;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class SnetServiceApiMojoTestIT {

    private static final File testProjectDir = new File("target/test-classes/project-to-test/");
    private SnetServiceApiMojo mojo;

    @BeforeEach
    void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(testProjectDir, "target"));

        mojo = new SnetServiceApiMojo();

        Field field = mojo.getClass().getDeclaredField("orgId");
        field.setAccessible(true);
        field.set(mojo, "example-org");

        field = mojo.getClass().getDeclaredField("serviceId");
        field.setAccessible(true);
        field.set(mojo, "example-service");

        field = mojo.getClass().getDeclaredField("outputDir");
        field.setAccessible(true);
        field.set(mojo, new File("build"));

        field = mojo.getClass().getDeclaredField("javaPackage");
        field.setAccessible(true);
        field.set(mojo, "io.singularitynet.service");

        field = mojo.getClass().getDeclaredField("ipfsRpcEndpoint");
        field.setAccessible(true);
        field.set(mojo, new URL("http://localhost:5002"));

        field = mojo.getClass().getDeclaredField("ethereumJsonRpcEndpoint");
        field.setAccessible(true);
        field.set(mojo, new URL("http://localhost:8545"));

        field = mojo.getClass().getDeclaredField("registryAddress");
        field.setAccessible(true);
        field.set(mojo, "0x4e74fefa82e83e0964f0d9f53c68e03f7298a8b2");

//        mojo.setOutputDir(outputDir);


    }

    @Test
    void getApiUsingRegistryAndIpfs() throws Exception {
        assertNotNull(mojo, "Mojo должен быть создан");
        mojo.execute();
        File outputDir = new File(testProjectDir, "target/generated-sources");

        assertNotNull(outputDir);
        assertTrue(outputDir.exists(), "Output dir doesn't exist");

        File protoFile = new File(outputDir, "example_service.proto");
        assertTrue(protoFile.exists(), "API не был загружен");

        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(protoFile))) {
            String protobuf = IOUtils.toString(is);
            assertTrue(protobuf.endsWith("option java_package = \"io.singularitynet.service\";\n"),
                    "Java package не добавлен");
        }
    }
}
