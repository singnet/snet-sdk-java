package io.singularitynet.sdk.mpe;

// TODO: cyclic dependency between mpe and client packages
import io.singularitynet.sdk.client.ServiceClient;

public interface PaymentStrategy {

    <ReqT, RespT> Payment getPayment(GrpcCallParameters<ReqT, RespT> parameters, ServiceClient serviceClient);

}
