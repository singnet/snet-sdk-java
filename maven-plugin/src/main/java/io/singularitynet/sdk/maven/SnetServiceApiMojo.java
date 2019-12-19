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
    @Parameter(defaultValue = "http://ipfs.singularitynet.io:80", property = "ipfsRpcEndpoint", required = true)
    private URL ipfsRpcEndpoint;

    @Getter
    @Parameter(defaultValue = "https://mainnet.infura.io", property = "ethereumJsonRpcEndpoint", required = true)
    private URL ethereumJsonRpcEndpoint;

    @Getter
    @Parameter(defaultValue = "0xdcE9c76cCB881AF94F7FB4FaC94E4ACC584fa9a5", property = "getterEthereumAddress", required = true)
    private String getterEthereumAddress;

    @Getter
    @Parameter(property = "registryAddress", required = false)
    private String registryAddress;

    public void execute() throws MojoExecutionException {
        try {
            new ServiceApiGetter(this).run();
        } catch (PluginException e) {
            throw new MojoExecutionException("Could not get API", e);
        }
    }

}
