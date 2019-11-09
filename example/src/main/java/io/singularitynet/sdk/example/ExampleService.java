package io.singularitynet.sdk.example;

import java.math.BigInteger;

import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.JsonConfiguration;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.FixedPaymentChannelPaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;

import example.service.CalculatorGrpc;
import example.service.CalculatorGrpc.CalculatorBlockingStub;
import example.service.ExampleService.Numbers;
import example.service.ExampleService.Result;

public class ExampleService {

    public static void main(String[] args) throws Exception {
        String privateKey = args[0];
        BigInteger channelId = new BigInteger(args[1]);

        String json = "{" +
            "\"ethereum_json_rpc_endpoint\": \"https://ropsten.infura.io\", " +
            "\"ipfs_url\": \"http://ipfs.singularitynet.io:80\"," +
            "\"signer_type\": \"PRIVATE_KEY\"," +
            "\"signer_private_key_base64\": \"" + hexToBase64(privateKey) + "\"" +
            "}";
        Configuration config = new JsonConfiguration(json);

        Sdk sdk = new Sdk(config);
        try {

            PaymentStrategy paymentStrategy = new FixedPaymentChannelPaymentStrategy(
                    channelId);
            ServiceClient serviceClient = sdk.newServiceClient("snet", "example-service",
                    "default_group", paymentStrategy); 
            try {

                CalculatorBlockingStub stub = serviceClient.getGrpcStub(CalculatorGrpc::newBlockingStub);

                Numbers numbers = Numbers.newBuilder()
                    .setA(7)
                    .setB(6)
                    .build();
                Result result = stub.mul(numbers);
                System.out.println("Response received: " + result);

            } finally {
                serviceClient.shutdownNow();
            }

        } finally {
            sdk.shutdown();
        }
    }

    private static String hexToBase64(String hex) {
        return io.singularitynet.sdk.common.Utils.bytesToBase64(
                io.singularitynet.sdk.common.Utils.hexToBytes(hex));
    }

}
