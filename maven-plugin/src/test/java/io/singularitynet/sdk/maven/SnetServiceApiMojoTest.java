package io.singularitynet.sdk.maven;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import org.apache.commons.io.FileUtils;

public class SnetServiceApiMojoTest
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
        assertTrue(outputDir.exists());

        File proto = new File(outputDir, "example_service.proto");
        assertTrue(proto.exists());
    }

}

