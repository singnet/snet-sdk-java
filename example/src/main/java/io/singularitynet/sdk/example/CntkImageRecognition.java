package io.singularitynet.sdk.example;

import java.math.BigInteger;

import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.JsonConfiguration;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.FixedPaymentChannelPaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;

import recognition.RecognizerGrpc;
import recognition.RecognizerGrpc.RecognizerBlockingStub;
import recognition.ImageRecon.Input;
import recognition.ImageRecon.Output;

public class CntkImageRecognition {

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
                    channelId, config.getSigner());
            ServiceClient serviceClient = sdk.newServiceClient("snet", "cntk-image-recon",
                    "default_group", paymentStrategy); 
            try {

                RecognizerBlockingStub stub = serviceClient.getGrpcStub(RecognizerGrpc::newBlockingStub);
                Input input = Input.newBuilder()
                    .setModel("ResNet152")
                    .setImgPath("https://d2z4fd79oscvvx.cloudfront.net/0027071_1_single_rose_385.jpeg")
                    .build();
                Output output = stub.flowers(input);
                System.out.println("Response received: " + output);

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
