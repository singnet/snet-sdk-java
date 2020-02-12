package io.singularitynet.sdk.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.ReadonlyTransactionManager;
import io.ipfs.api.IPFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.ContractUtils;
import io.singularitynet.sdk.registry.IpfsMetadataStorage;
import io.singularitynet.sdk.registry.RegistryMetadataProvider;
import io.singularitynet.sdk.registry.ServiceMetadata;
import io.singularitynet.sdk.registry.RegistryContract;

public class ServiceApiGetter {

    public static interface Parameters {

        String getOrgId();
        String getServiceId();
        File getOutputDir();
        String getJavaPackage();
        URL getIpfsRpcEndpoint();
        URL getEthereumJsonRpcEndpoint();
        String getGetterEthereumAddress();
        String getRegistryAddress();

    }

    public static abstract class DefaultParameters implements Parameters {

        public URL getIpfsRpcEndpoint() {
            return asURL(DEFAULT_IPFS_ENDPOINT);
        }

        public URL getEthereumJsonRpcEndpoint() {
            return asURL(DEFAULT_ETHEREUM_JSON_RPC_ENDPOINT);
        }

        public String getGetterEthereumAddress() {
            return DEFAULT_GETTER_ETHEREUM_ADDRESS;
        }

        public String getRegistryAddress() {
            return DEFAULT_REGISTRY_ADDRESS;
        }

        private static URL asURL(String url) {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Unexpected exception", e);
            }
        }
    }

    public static final String DEFAULT_IPFS_ENDPOINT = "http://ipfs.singularitynet.io:80";
    //FIXME: make it required field
    public static final String DEFAULT_ETHEREUM_JSON_RPC_ENDPOINT = "https://mainnet.infura.io";
    public static final String DEFAULT_GETTER_ETHEREUM_ADDRESS = "0xdcE9c76cCB881AF94F7FB4FaC94E4ACC584fa9a5";
    public static final String DEFAULT_REGISTRY_ADDRESS = "";

    private final static Logger log = LoggerFactory.getLogger(ServiceApiGetter.class);

    private final Parameters parameters;

    private Registry registry;
    private IPFS ipfs;

    public ServiceApiGetter(Parameters parameters) {
        this(null, null, parameters);
    }

    ServiceApiGetter(Registry registry, IPFS ipfs, Parameters parameters) {
        this.registry = registry;
        this.ipfs = ipfs;
        this.parameters = parameters;
    }

    public void run() throws PluginException {
        log.info("Downloading API of orgId: {}, serviceId: {}, javaPackage: {}, into: {}, ethereumJsonRpcEndpoint: {}",
                parameters.getOrgId(), parameters.getServiceId(),
                parameters.getJavaPackage(), parameters.getOutputDir(),
                parameters.getEthereumJsonRpcEndpoint());
        log.debug("ipfsRpcEndpoint: {}, getterEthereumAddress: {}, registryAddress: {}",
                parameters.getIpfsRpcEndpoint(), parameters.getGetterEthereumAddress(),
                (parameters.getRegistryAddress() == null ? "<network default>" : parameters.getRegistryAddress()));

        if (ipfs == null) {
            Web3j web3j = Web3j.build(new HttpService(parameters.getEthereumJsonRpcEndpoint().toExternalForm()));
            try {
                web3j.ethBlockNumber().send();
                registry = getRegistryContract(web3j);
                ipfs = new IPFS(parameters.getIpfsRpcEndpoint().getHost(),
                        parameters.getIpfsRpcEndpoint().getPort());
                runInternal();
            } catch (IOException | ClientConnectionException e) {
                throw new PluginException("Could not perform operation on Ethereum RPC endpoint provided: "
                        + parameters.getEthereumJsonRpcEndpoint(), e);
            } finally {
                web3j.shutdown();
            }
        } else {
            runInternal();
        }
    }

    private void runInternal() throws PluginException {
        RegistryContract registryContract = new RegistryContract(registry);
        IpfsMetadataStorage metadataStorage = new IpfsMetadataStorage(ipfs);
        RegistryMetadataProvider metadataProvider = new RegistryMetadataProvider(
                parameters.getOrgId(), parameters.getServiceId(),
                registryContract, metadataStorage);
        ServiceMetadata metadata = metadataProvider.getServiceMetadata();
        log.debug("service metadata: {}", metadata);
        loadAndUnpackApi(metadataStorage, metadata.getModelIpfsHash());
    }

    private Registry getRegistryContract(Web3j web3j) throws PluginException {
        String networkId;
        try {
            networkId = web3j.netVersion().send().getNetVersion();
        } catch (IOException e) {
            throw new PluginException("Could not get Ethereum network id", e);
        }
        DefaultGasProvider gasProvider = new DefaultGasProvider();
        ReadonlyTransactionManager transactionManager = new ReadonlyTransactionManager(
                web3j, new Address(parameters.getGetterEthereumAddress()).toString());
        String registryAddress = parameters.getRegistryAddress();
        if (registryAddress == null || registryAddress.isEmpty()) {
            registryAddress = ContractUtils.readContractAddress(networkId, "Registry").toString();
        }
        Registry registry = Registry.load(new Address(registryAddress).toString(),
                web3j, transactionManager, gasProvider);
        return registry;
    }

    private void loadAndUnpackApi(IpfsMetadataStorage metadataStorage, String ipfsHash) throws PluginException {
        URI uri;
        try {
            uri = new URI("ipfs://" + ipfsHash);
            log.info("IPFS uri: {}", uri);
        } catch (URISyntaxException e) {
            throw new PluginException("Incorrect IPFS hash in metadata: " + ipfsHash, e);
        }
        byte[] apiTar = metadataStorage.get(uri);
        log.info("API size {} bytes", apiTar.length);
        unpackApi(apiTar);
    }

    private void unpackApi(byte[] apiTar) throws PluginException {
        ArchiveInputStream is = new TarArchiveInputStream(
                new ByteArrayInputStream(apiTar));
        try {
            ArchiveEntry entry = null;
            while ((entry = is.getNextEntry()) != null) {
                if (!is.canReadEntryData(entry)) {
                    throw new PluginException("Cannot read entry in API archive: " + entry);
                }
                log.info("Unpacking {}", entry.getName());
                String name = new File(parameters.getOutputDir(), entry.getName()).getAbsolutePath();
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
                            o.write(("\noption java_package = \"" + parameters.getJavaPackage() + "\";\n").getBytes());
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new PluginException("Could not download and unpack API", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new PluginException("Could not close archive unpacked", e);
            }
        }
    }

}
