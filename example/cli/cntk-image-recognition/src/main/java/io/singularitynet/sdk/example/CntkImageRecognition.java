package io.singularitynet.sdk.example;

import java.util.Properties;

import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.ConfigurationUtils;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.paymentstrategy.OnDemandPaymentChannelPaymentStrategy;

import io.singularitynet.service.cntkimagerecon.RecognizerGrpc;
import io.singularitynet.service.cntkimagerecon.RecognizerGrpc.RecognizerBlockingStub;
import io.singularitynet.service.cntkimagerecon.ImageRecon.Input;
import io.singularitynet.service.cntkimagerecon.ImageRecon.Output;

public class CntkImageRecognition {

    public static void main(String[] args) throws Exception {
        String privateKey = args[0];

        Properties props = new Properties();
        props.load(CntkImageRecognition.class.getClassLoader()
                .getResourceAsStream("ethereum.properties"));
        props.setProperty("identity.type", "PRIVATE_KEY");
        props.setProperty("identity.private.key.hex", privateKey);
        Configuration config = ConfigurationUtils.fromProperties(props);

        Sdk sdk = new Sdk(config);
        try {

            PaymentStrategy paymentStrategy =
                new OnDemandPaymentChannelPaymentStrategy(sdk);
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
                serviceClient.shutdownNow();
            }

        } finally {
            sdk.shutdown();
        }
    }

}
