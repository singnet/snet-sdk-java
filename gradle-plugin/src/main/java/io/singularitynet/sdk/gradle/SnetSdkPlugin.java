package io.singularitynet.sdk.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.net.URL;
import java.net.MalformedURLException;

public class SnetSdkPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().create("getSingularityNetServiceApi",
                GetSingularityNetServiceApi.class, (task) -> {
                    try {
                        // TODO: set default output dir
                        task.setIpfsRpcEndpoint(new URL("http://ipfs.singularitynet.io:80"));
                        task.setEthereumJsonRpcEndpoint(new URL( "https://mainnet.infura.io"));
                        task.setGetterEthereumAddress( "0xdcE9c76cCB881AF94F7FB4FaC94E4ACC584fa9a5");
                        task.setRegistryAddress("");
                    } catch(MalformedURLException e) {
                        throw new RuntimeException("Incorrect URL syntax", e);
                    }
                });
    }

}
