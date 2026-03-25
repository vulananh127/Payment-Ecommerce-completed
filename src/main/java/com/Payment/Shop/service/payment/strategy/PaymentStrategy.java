package com.Payment.Shop.service.payment.strategy;

import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.dto.request.PaymentRequest;
import com.Payment.Shop.dto.request.SavePaymentRequest;
import com.Payment.Shop.dto.response.BasePaymentResponse;
import com.Payment.Shop.entity.Payment;
import com.Payment.Shop.entity.Order;
import com.Payment.Shop.exception.PaymentException;
import com.Payment.Shop.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

// Abstract class for Payment Strategy

@Slf4j
public abstract class PaymentStrategy {

    protected PaymentRepository paymentRepository;


    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }


    abstract BasePaymentResponse processPayment(PaymentRequest paymentRequest) throws PaymentException;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePayment(SavePaymentRequest paymentRequest) {

        Order order = new Order(paymentRequest.getOrderId());
        // dùng constructor bạn đã có trong Order

        Payment payment = Payment.builder()
                .paymentMethod(paymentRequest.getPaymentMethod())
                .order(order)
                // .createdAt(paymentRequest.getCreatedAt())
                .amount(paymentRequest.getTotalAmount())
                .build();

        if(paymentRequest.getPaymentMethod().equals(PaymentMethod.VNPAY)){
            payment.setTransactionId(paymentRequest.getTransactionId());
        }

        try{
            paymentRepository.save(payment);
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }


}
