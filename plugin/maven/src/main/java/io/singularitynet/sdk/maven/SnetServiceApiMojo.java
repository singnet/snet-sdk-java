package io.singularitynet.sdk.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.net.URL;
import lombok.Getter;

import io.singularitynet.sdk.plugin.ServiceApiGetter;
import io.singularitynet.sdk.plugin.PluginException;

@Mojo(name = "get", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class SnetServiceApiMojo extends AbstractMojo implements ServiceApiGetter.Parameters {

    @Getter
    @Parameter(property = "orgId", required = true)
    private String orgId;

    @Getter
    @Parameter(property = "serviceId", required = true)
    private String serviceId;

    @Getter
    @Parameter(defaultValue = "${project.build.directory}/proto", property = "outputDir", required = true)
    private File outputDir;

    @Getter
    @Parameter(property = "javaPackage", required = true)
    private String javaPackage;

    @Getter
    @Parameter(defaultValue = ServiceApiGetter.DEFAULT_IPFS_ENDPOINT, property = "ipfsRpcEndpoint", required = true)
    private URL ipfsRpcEndpoint;

    @Getter
    @Parameter(property = "ethereumJsonRpcEndpoint", required = true)
    private URL ethereumJsonRpcEndpoint;

    @Getter
    @Parameter(defaultValue = ServiceApiGetter.DEFAULT_GETTER_ETHEREUM_ADDRESS, property = "getterEthereumAddress", required = true)
    private String getterEthereumAddress;

    @Getter
    @Parameter(defaultValue = ServiceApiGetter.DEFAULT_REGISTRY_ADDRESS, property = "registryAddress", required = false)
    private String registryAddress;

    public void execute() throws MojoExecutionException {
        try {
            new ServiceApiGetter(this).run();
        } catch (PluginException e) {
            throw new MojoExecutionException("Could not get API", e);
        }
    }

}
