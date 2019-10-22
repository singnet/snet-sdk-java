package io.singularitynet.sdk.mpe;

public interface PaymentStrategy {

    <ReqT, RespT> Payment getPayment(GrpcCallParameters<ReqT, RespT> parameters);

}
