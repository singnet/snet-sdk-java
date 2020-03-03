package io.singularitynet.sdk.client;

import io.singularitynet.sdk.payment.Payment;

// FIXME: add javadoc
public class CombinedPaymentStrategy implements PaymentStrategy {

    private final PaymentStrategy[] strategies;

    public CombinedPaymentStrategy(PaymentStrategy... strategies) {
        this.strategies = strategies;
    }

    @Override
    public <ReqT, RespT> Payment getPayment(GrpcCallParameters<ReqT, RespT> parameters, ServiceClient serviceClient) {
        for (PaymentStrategy strategy : strategies) {
            Payment payment = strategy.getPayment(parameters, serviceClient);
            if (payment != Payment.INVALID_PAYMENT) {
                return payment;
            }
        }
        return Payment.INVALID_PAYMENT; 
    }

}
