package io.singularitynet.sdk.example;

import java.math.BigInteger;
import java.util.Properties;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.ConfigurationUtils;
import io.singularitynet.sdk.client.StaticConfiguration;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.FixedPaymentChannelPaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;

import io.singularitynet.service.cntkimagerecon.RecognizerGrpc;
import io.singularitynet.service.cntkimagerecon.RecognizerGrpc.RecognizerBlockingStub;
import io.singularitynet.service.cntkimagerecon.ImageRecon.Input;
import io.singularitynet.service.cntkimagerecon.ImageRecon.Output;

public class CntkImageRecognition {

    public static void main(String[] args) throws Exception {
        String privateKey = args[0];
        BigInteger channelId = new BigInteger(args[1]);

        Properties props = new Properties();
        props.load(CntkImageRecognition.class.getClassLoader()
                .getResourceAsStream("ethereum.properties"));

        StaticConfiguration config = StaticConfiguration.newBuilder(
                ConfigurationUtils.fromProperties(props))
            .setIdentityType(Configuration.IdentityType.PRIVATE_KEY)
            .setIdentityPrivateKey(Utils.hexToBytes(privateKey))
            .build();

        Sdk sdk = new Sdk(config);
        try {

            PaymentStrategy paymentStrategy = new FixedPaymentChannelPaymentStrategy(
                    sdk, channelId);
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

}
