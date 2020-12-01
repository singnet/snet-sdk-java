package io.singularitynet.sdk.example;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.paymentstrategy.OnDemandPaymentChannelPaymentStrategy;

import io.singularitynet.service.cntkimagerecon.RecognizerGrpc;
import io.singularitynet.service.cntkimagerecon.RecognizerGrpc.RecognizerBlockingStub;
import io.singularitynet.service.cntkimagerecon.ImageRecon.Input;
import io.singularitynet.service.cntkimagerecon.ImageRecon.Output;

public class CntkImageRecognition {

    public static void main(String[] args) throws Exception {
        String privateKey = args[0];

        Configuration config = Configuration.newBuilder()
            .setEthereumJsonRpcEndpoint(Configuration.MAINNET_INFURA_ETHEREUM_JSON_RPC_ENDPOINT)
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(Utils.hexToBytes(privateKey))
            .build();

        Sdk sdk = new Sdk(config);
        try {

            OnDemandPaymentChannelPaymentStrategy paymentStrategy =
                new OnDemandPaymentChannelPaymentStrategy(40320 /* about a week in Ethereum blocks */, 100);

            ServiceClient serviceClient = sdk.newServiceClient("snet",
                    "cntk-image-recon", "default_group", paymentStrategy);
            try {

                RecognizerBlockingStub stub = serviceClient.getGrpcStub(RecognizerGrpc::newBlockingStub);
                Input input = Input.newBuilder()
                    .setModel("ResNet152")
                    .setImgPath("https://d2z4fd79oscvvx.cloudfront.net/0027071_1_single_rose_385.jpeg")
                    .build();
                Output output = stub.flowers(input);
                System.out.println("Response received: " + output);

            } finally {
                serviceClient.close();
            }

        } finally {
            sdk.close();
        }
    }

}
