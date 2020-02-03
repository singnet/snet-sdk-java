package io.singularitynet.sdk.integration;

import org.junit.*;
import static org.junit.Assert.*;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tuples.generated.*;

import io.singularitynet.sdk.contracts.Registry;
import io.singularitynet.sdk.contracts.MultiPartyEscrow;
import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.mpe.MultiPartyEscrowContract;
import io.singularitynet.sdk.mpe.PaymentChannel;

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
        Registry registry = Registry.load(IntEnv.REGISTRY_CONTRACT_ADDRESS.toString(), web3j,
                roTransactionManager, gasProvider);

        Tuple4<Boolean, byte[], byte[], List<byte[]>> result = 
            registry.getServiceRegistrationById(Utils.strToBytes32(IntEnv.TEST_ORG_ID),
                    Utils.strToBytes32(IntEnv.TEST_SERVICE_ID)).send();

        assertTrue(result.getValue1());
    }

    @Test(timeout=5000)
    public void getOpenChannelEvents() throws Exception {
        MultiPartyEscrowContract mpe = new MultiPartyEscrowContract(web3j,
                MultiPartyEscrow.load(IntEnv.MPE_CONTRACT_ADDRESS.toString(),
                    web3j, roTransactionManager, gasProvider));

        List<PaymentChannel> channels = mpe.getChannelOpenEvents()
            .collect(Collectors.toList());

        assertTrue(channels.size() > 0);
    }

    @Test
    public void ethBlockNumber() throws Exception {
        BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();

        assertNotNull(blockNumber);
    }

}
