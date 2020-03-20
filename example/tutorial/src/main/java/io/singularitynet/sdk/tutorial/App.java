package io.singularitynet.sdk.tutorial;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.paymentstrategy.OnDemandPaymentChannelPaymentStrategy;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.client.Sdk;

import io.singularitynet.service.exampleservice.CalculatorGrpc;
import io.singularitynet.service.exampleservice.CalculatorGrpc.CalculatorBlockingStub;
import io.singularitynet.service.exampleservice.ExampleService.Numbers;
import io.singularitynet.service.exampleservice.ExampleService.Result;

public class App {

    public static void main( String[] args ) {
        Configuration config = Configuration.newBuilder()
            .setEthereumJsonRpcEndpoint("http://localhost:8545")
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(Utils.hexToBytes("04899d5fd471ce68f84a5ec64e2e4b6b045d8b850599a57f5b307024be01f262"))
            // for the custom environment only
            .setIpfsEndpoint("http://localhost:5002")
            .setRegistryAddress(new Address("0x4e74fefa82e83e0964f0d9f53c68e03f7298a8b2"))
            .setMultiPartyEscrowAddress(new Address("0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e"))
            .build();

        Sdk sdk = new Sdk(config);
        try {

            // 40320 is a week in Ethereum blocks assuming single block is mined in 15 seconds
            OnDemandPaymentChannelPaymentStrategy paymentStrategy =
                new OnDemandPaymentChannelPaymentStrategy(sdk, 40320, 100);

            ServiceClient serviceClient = sdk.newServiceClient("example-org",
                    "example-service", "default_group", paymentStrategy);
            try {

                CalculatorBlockingStub stub = serviceClient.getGrpcStub(CalculatorGrpc::newBlockingStub);
                Numbers numbers = Numbers.newBuilder()
                    .setA(7)
                    .setB(6)
                    .build();
                Result result = stub.mul(numbers);
                System.out.println("Response received: " + result);

            } finally {
                serviceClient.close();
            }
        } finally {
            sdk.close();
        }
    }

}
