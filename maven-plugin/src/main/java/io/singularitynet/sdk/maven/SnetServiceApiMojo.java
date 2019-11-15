package io.singularitynet.sdk.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Optional;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.ReadonlyTransactionManager;
import io.ipfs.api.IPFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.ContractUtils;
import io.singularitynet.sdk.registry.IpfsMetadataStorage;
import io.singularitynet.sdk.registry.RegistryMetadataProvider;
import io.singularitynet.sdk.registry.ServiceMetadata;
import io.singularitynet.sdk.registry.RegistryContract;

@Mojo(name = "get", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class SnetServiceApiMojo extends AbstractMojo
{
    private final static Logger log = LoggerFactory.getLogger(SnetServiceApiMojo.class);

    @Parameter(property = "orgId", required = true)
    private String orgId;

    @Parameter(property = "serviceId", required = true)
    private String serviceId;

    @Parameter(defaultValue = "${project.build.directory}/proto", property = "outputDir", required = true)
    private File outputDir;

    @Parameter(property = "javaPackage", required = true)
    private String javaPackage;

    @Parameter(defaultValue = "http://ipfs.singularitynet.io:80", property = "ipfsRpcEndpoint", required = true)
    private URL ipfsRpcEndpoint;

    @Parameter(defaultValue = "https://mainnet.infura.io", property = "ethereumJsonRpcEndpoint", required = true)
    private URL ethereumJsonRpcEndpoint;

    @Parameter(defaultValue = "0xdcE9c76cCB881AF94F7FB4FaC94E4ACC584fa9a5", property = "getterEthereumAddress", required = true)
    private String getterEthereumAddress;

    @Parameter(property = "registryAddress", required = false)
    private String registryAddress;

    public void execute() throws MojoExecutionException {
        log.info("Downloading API of orgId: {}, serviceId: {}, javaPackage: {}, into: {}",
                orgId, serviceId, javaPackage, outputDir);
        log.debug("ethereumJsonRpcEndpoint: {}, ipfsRpcEndpoint: {}, " +
                "getterEthereumAddress: {}, registryAddress: {}",
                ethereumJsonRpcEndpoint, ipfsRpcEndpoint, getterEthereumAddress,
                (registryAddress == null ? "<network default>" : registryAddress));
        Web3j web3j = Web3j.build(new HttpService(ethereumJsonRpcEndpoint.toExternalForm()));
        try {
            RegistryContract registryContract = getRegistryContract(web3j);
            IPFS ipfs = new IPFS(ipfsRpcEndpoint.getHost(), ipfsRpcEndpoint.getPort());
            IpfsMetadataStorage metadataStorage = new IpfsMetadataStorage(ipfs);
            RegistryMetadataProvider metadataProvider = new RegistryMetadataProvider(
                    orgId, serviceId, registryContract, metadataStorage);
            ServiceMetadata metadata = metadataProvider.getServiceMetadata();
            log.debug("service metadata: {}", metadata);
            loadAndUnpackApi(metadataStorage, metadata.getModelIpfsHash());
        } finally {
            web3j.shutdown();
        }
    }

    private RegistryContract getRegistryContract(Web3j web3j) throws MojoExecutionException {
        String networkId;
        try {
            networkId = web3j.netVersion().send().getNetVersion();
        } catch (IOException e) {
            throw new MojoExecutionException("Could not get Ethereum network id", e);
        }
        DefaultGasProvider gasProvider = new DefaultGasProvider();
        ReadonlyTransactionManager transactionManager = new ReadonlyTransactionManager(
                web3j, new Address(getterEthereumAddress).toString());
        if (registryAddress == null) {
            registryAddress = ContractUtils.readContractAddress(networkId, "Registry").toString();
        }
        Registry registry = Registry.load(new Address(registryAddress).toString(), web3j,
                transactionManager, gasProvider);
        return new RegistryContract(registry);
    }

    private void loadAndUnpackApi(IpfsMetadataStorage metadataStorage, String ipfsHash) throws MojoExecutionException {
        URI uri;
        try {
            uri = new URI("ipfs://" + ipfsHash);
            log.info("IPFS uri: {}", uri);
        } catch (URISyntaxException e) {
            throw new MojoExecutionException("Incorrect IPFS hash in metadata: " + ipfsHash, e);
        }
        byte[] apiTar = metadataStorage.get(uri);
        log.info("API size {} bytes", apiTar.length);
        unpackApi(apiTar);
    }

    private void unpackApi(byte[] apiTar) throws MojoExecutionException {
        ArchiveInputStream is = new TarArchiveInputStream(
                new ByteArrayInputStream(apiTar));
        try {
            ArchiveEntry entry = null;
            while ((entry = is.getNextEntry()) != null) {
                if (!is.canReadEntryData(entry)) {
                    throw new MojoExecutionException("Cannot read entry in API archive: " + entry);
                }
                log.info("Unpacking {}", entry.getName());
                String name = new File(outputDir, entry.getName()).getAbsolutePath();
                File f = new File(name);
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("failed to create directory " + f);
                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("failed to create directory " + parent);
                    }
                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
                        IOUtils.copy(is, o);
                        if (name.endsWith(".proto")) {
                            log.debug("Adding package to protobuf file: {}", name);
                            o.write(("\noption java_package = \"" + javaPackage + "\";").getBytes());
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not download and unpack API", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new MojoExecutionException("Could not close archive unpacked", e);
            }
        }
    }
}
