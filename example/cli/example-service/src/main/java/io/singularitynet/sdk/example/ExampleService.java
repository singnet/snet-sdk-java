package io.singularitynet.sdk.example;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.ConfigurationUtils;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.paymentstrategy.OnDemandPaymentChannelPaymentStrategy;

import io.singularitynet.service.exampleservice.CalculatorGrpc;
import io.singularitynet.service.exampleservice.CalculatorGrpc.CalculatorBlockingStub;
import io.singularitynet.service.exampleservice.ExampleService.Numbers;
import io.singularitynet.service.exampleservice.ExampleService.Result;

public class ExampleService {

    public static void main(String[] args) throws Exception {
        String privateKey = args[0];

        Configuration config = Configuration.newBuilder()
            .setEthereumJsonRpcEndpoint(Configuration.ROPSTEN_INFURA_ETHEREUM_JSON_RPC_ENDPOINT)
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(Utils.hexToBytes(privateKey))
            .build();

        Sdk sdk = new Sdk(config);
        try {

            OnDemandPaymentChannelPaymentStrategy paymentStrategy =
                new OnDemandPaymentChannelPaymentStrategy(40320 /* about a week in Ethereum blocks */, 100);

            ServiceClient serviceClient = sdk.newServiceClient("snet",
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
