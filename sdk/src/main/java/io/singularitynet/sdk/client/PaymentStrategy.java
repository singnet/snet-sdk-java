package io.singularitynet.sdk.client;

import io.singularitynet.sdk.payment.Payment;

/**
 * The strategy provides a payment for the client call.
 */
public interface PaymentStrategy {

    /**
     * Return the payment for the client call.
     * @param <ReqT> type of the gRPC request of the call.
     * @param <RespT> type of the gRPC response of the call.
     * @param parameters provides the information about the gRPC call context.
     * @param serviceClient provides the information about the platform service
     * context.
     * @return instance of the Payment class.
     */
    <ReqT, RespT> Payment getPayment(GrpcCallParameters<ReqT, RespT> parameters, ServiceClient serviceClient);

}
