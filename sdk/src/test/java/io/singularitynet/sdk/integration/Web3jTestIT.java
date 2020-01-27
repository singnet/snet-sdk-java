package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.List;
import java.math.BigInteger;
import java.io.IOException;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tuples.generated.*;

import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.common.Utils;

public class Web3jTestIT {

    private Web3j web3j;
    private ReadonlyTransactionManager roTransactionManager;
    private DefaultGasProvider gasProvider;

    @Before
    public void setUp() {
        web3j = Web3j.build(new HttpService("http://localhost:8545"));
        roTransactionManager = new ReadonlyTransactionManager(
                web3j, "0x008f312C5635a66c0fB49952D7C431D765bb3D3c");
        gasProvider = new DefaultGasProvider();
    }

    @Test
    public void getServiceRegistrationById() throws Exception {
        Registry registry = Registry.load("0x4e74fefa82e83e0964f0d9f53c68e03f7298a8b2", web3j,
                roTransactionManager, gasProvider);

        Tuple4<Boolean, byte[], byte[], List<byte[]>> result = 
            registry.getServiceRegistrationById(Utils.strToBytes32("example-org"),
                    Utils.strToBytes32("example-service")).send();

        assertTrue(result.getValue1());
    }

    @Test
    public void ethBlockNumber() throws IOException {
        BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();

        assertNotNull(blockNumber);
    }

}
