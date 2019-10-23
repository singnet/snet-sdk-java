package io.singularitynet.sdk.client;

import io.singularitynet.sdk.mpe.Payment;

public interface PaymentStrategy {

    <ReqT, RespT> Payment getPayment(GrpcCallParameters<ReqT, RespT> parameters, ServiceClient serviceClient);

}
