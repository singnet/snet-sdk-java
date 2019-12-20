package io.singularitynet.sdk.gradle;

import org.gradle.api.Task;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.net.URL;
import lombok.Setter;

import io.singularitynet.sdk.plugin.ServiceApiGetter;
import io.singularitynet.sdk.plugin.PluginException;

public class GetSingularityNetServiceApi extends DefaultTask implements ServiceApiGetter.Parameters {

    @Setter
    private String orgId;
    @Setter
    private String serviceId;
    @Setter
    private File outputDir;
    @Setter
    private String javaPackage;
    @Setter
    private URL ipfsRpcEndpoint;
    @Setter
    private URL ethereumJsonRpcEndpoint;
    @Setter
    private String getterEthereumAddress;
    @Setter
    private String registryAddress;

    public GetSingularityNetServiceApi() {
        try {
            ipfsRpcEndpoint = new URL(ServiceApiGetter.DEFAULT_IPFS_ENDPOINT);
            ethereumJsonRpcEndpoint = new URL(ServiceApiGetter.DEFAULT_ETHEREUM_JSON_RPC_ENDPOINT);
            getterEthereumAddress = ServiceApiGetter.DEFAULT_GETTER_ETHEREUM_ADDRESS;
            registryAddress = ServiceApiGetter.DEFAULT_REGISTRY_ADDRESS;
        } catch (java.net.MalformedURLException e) {
            throw new IllegalStateException("Unexpected error", e);
        }

        for (Task task : getProject().getTasksByName("preBuild", false)) {
            task.dependsOn(this);
        }
    }

    @Input
    public String getOrgId() {
        return orgId;
    }

    @Input
    public String getServiceId() {
        return serviceId;
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    @Input
    public String getJavaPackage() {
        return javaPackage;
    }

    @Input
    public URL getIpfsRpcEndpoint() {
        return ipfsRpcEndpoint;
    }

    @Input
    public URL getEthereumJsonRpcEndpoint() {
        return ethereumJsonRpcEndpoint;
    }

    @Input
    public String getGetterEthereumAddress() {
        return getterEthereumAddress;
    }

    @Input
    public String getRegistryAddress() {
        return registryAddress;
    }

    @TaskAction
    void getSingularityNetServiceApi() {
        try {
            new ServiceApiGetter(this).run();
        } catch (PluginException e) {
            throw new RuntimeException("Could not get API", e);
        }
    }

}
