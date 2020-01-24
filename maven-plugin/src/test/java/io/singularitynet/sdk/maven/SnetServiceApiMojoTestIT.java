package io.singularitynet.sdk.maven;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class SnetServiceApiMojoTestIT
{
    // TODO: use org.junit.Assume to run tests on local Ethereum environment
    // when it is started

    private static final File testProjectDir = new File("target/test-classes/project-to-test/");

    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
            FileUtils.deleteDirectory(new File(testProjectDir, "target"));
        }

        @Override
        protected void after() {
        }
    };

    @Test
    public void getApiUsingRegistryAndIpfs() throws Exception {
        SnetServiceApiMojo mojo = (SnetServiceApiMojo) rule.lookupConfiguredMojo(testProjectDir, "get");
        assertNotNull(mojo);
        mojo.execute();

        File outputDir = (File) rule.getVariableValueFromObject(mojo, "outputDir");
        assertNotNull(outputDir);
        assertTrue("Output dir doesn't exist", outputDir.exists());

        File protoFile = new File(outputDir, "example_service.proto");
        assertTrue("API is not downloaded", protoFile.exists());

        BufferedInputStream is = new BufferedInputStream(new FileInputStream(protoFile));
        try {
            String protobuf = IOUtils.toString(is);
            assertTrue("Java package is not added", protobuf.endsWith("option java_package = \"io.singularitynet.service\";\n"));
        } finally {
            is.close();
        }
    }

}

