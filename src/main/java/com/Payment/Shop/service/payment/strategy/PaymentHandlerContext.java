package com.Payment.Shop.service.payment.strategy;

import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.dto.request.PaymentRequest;
import com.Payment.Shop.dto.response.BasePaymentResponse;
import com.Payment.Shop.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// This is Context
@Component
@Slf4j
public class PaymentHandlerContext {

    private PaymentStrategy paymentStrategy;

    private final VNPayStrategy vnPayStrategy;
    private final StripePaymentStrategy stripePaymentStrategy;

    public PaymentHandlerContext(VNPayStrategy vnPayStrategy, StripePaymentStrategy stripePaymentStrategy) {
        this.vnPayStrategy = vnPayStrategy;
        this.stripePaymentStrategy = stripePaymentStrategy;
    }

    private PaymentStrategy getPaymentStrategy(PaymentMethod paymentMethod){

        return switch (paymentMethod) {
            case COD -> null;
            case VNPAY -> vnPayStrategy;
            case MOMO -> null;
            case BANKING -> null;
            case STRIPE -> stripePaymentStrategy;
            default -> throw new RuntimeException("Unsupported payment method");
        };


    }
    public BasePaymentResponse executePayment(PaymentRequest paymentRequest){

        this.paymentStrategy = this.getPaymentStrategy(paymentRequest.getPaymentMethod());

        try {
            return paymentStrategy.processPayment(paymentRequest);

        }catch (PaymentException exception){
            log.error("Payment exception: {}", exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
