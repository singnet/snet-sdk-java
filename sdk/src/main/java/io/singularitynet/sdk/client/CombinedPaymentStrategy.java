package io.singularitynet.sdk.client;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.payment.Payment;

/**
 * Payment strategy which combines a list of the underlying strategies. It gets
 * payment from each strategy in the list until one of the returns valid
 * payment. First valid payment is returned. If all strategies returned
 * Payment.INVALID_PAYMENT then it is returned.
 */
@ToString
public class CombinedPaymentStrategy implements PaymentStrategy {

    private final static Logger log = LoggerFactory.getLogger(CombinedPaymentStrategy.class);

    private final PaymentStrategy[] strategies;

    /**
     * Constructor.
     * @param strategies list of strategies to be applied until first valid
     * payment is received.
     */
    public CombinedPaymentStrategy(PaymentStrategy... strategies) {
        this.strategies = strategies;
    }

    @Override
    public <ReqT, RespT> Payment getPayment(GrpcCallParameters<ReqT, RespT> parameters, ServiceClient serviceClient) {
        for (PaymentStrategy strategy : strategies) {
            log.debug("Try payment strategy: {}", strategy);
            Payment payment = strategy.getPayment(parameters, serviceClient);
            if (payment != Payment.INVALID_PAYMENT) {
                return payment;
            }
        }
        return Payment.INVALID_PAYMENT; 
    }

}
