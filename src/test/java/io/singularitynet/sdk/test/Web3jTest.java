package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.List;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tuples.generated.*;

import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.common.Utils;

public class Web3jTest {

    private Web3j web3j;
    private ReadonlyTransactionManager roTransactionManager;
    private DefaultGasProvider gasProvider;

    @Before
    public void setUp() {
        web3j = Web3j.build(new HttpService("https://ropsten.infura.io"));
        roTransactionManager = new ReadonlyTransactionManager(
                web3j, "0x008f312C5635a66c0fB49952D7C431D765bb3D3c");
        gasProvider = new DefaultGasProvider();
    }

    @Test
    public void getServiceRegistrationById() throws Exception {
        Registry registry = Registry.load("0x663422c6999Ff94933DBCb388623952CF2407F6f", web3j,
                roTransactionManager, gasProvider);

        Tuple4<Boolean, byte[], byte[], List<byte[]>> result = 
            registry.getServiceRegistrationById(Utils.strToBytes32("snet"),
                    Utils.strToBytes32("speech-recognition")).send();

        assertTrue(result.component1());
    }

}
