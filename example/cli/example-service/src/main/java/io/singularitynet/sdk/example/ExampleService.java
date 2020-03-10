package io.singularitynet.sdk.example;

import java.util.Properties;

import io.singularitynet.sdk.client.Configuration;
import io.singularitynet.sdk.client.ConfigurationUtils;
import io.singularitynet.sdk.client.Sdk;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.sdk.paymentstrategy.OnDemandPaymentChannelPaymentStrategy;

import io.singularitynet.service.exampleservice.CalculatorGrpc;
import io.singularitynet.service.exampleservice.CalculatorGrpc.CalculatorBlockingStub;
import io.singularitynet.service.exampleservice.ExampleService.Numbers;
import io.singularitynet.service.exampleservice.ExampleService.Result;

public class ExampleService {

    public static void main(String[] args) throws Exception {
        String privateKey = args[0];

        Properties props = new Properties();
        props.load(ExampleService.class.getClassLoader()
                .getResourceAsStream("ethereum.properties"));
        props.setProperty("identity.type", "PRIVATE_KEY");
        props.setProperty("identity.private.key.hex", privateKey);
        Configuration config = ConfigurationUtils.fromProperties(props);

        Sdk sdk = new Sdk(config);
        try {

            PaymentStrategy paymentStrategy =
                new OnDemandPaymentChannelPaymentStrategy(sdk);
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
