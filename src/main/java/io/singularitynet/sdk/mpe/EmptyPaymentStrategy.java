package io.singularitynet.sdk.mpe;

public class EmptyPaymentStrategy implements PaymentStrategy {

    @Override
    public <ReqT, RespT> Payment getPayment(GrpcCallParameters<ReqT, RespT> callParams) {
        return new EscrowPayment();
    }

}
