package com.Payment.Shop.controller;


import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.dto.ResultObject;
import com.Payment.Shop.dto.request.PaymentRequest;
import com.Payment.Shop.dto.response.BasePaymentResponse;
import com.Payment.Shop.dto.response.IpnResponse;
import com.Payment.Shop.service.payment.IpnHandler.IpnHandlerFactory;
import com.Payment.Shop.service.payment.strategy.PaymentHandlerContext;
import com.Payment.Shop.util.RequestUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
public class PaymentController {

    private final PaymentHandlerContext paymentHandler;
    private final IpnHandlerFactory ipnHandlerFactory;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public PaymentController(PaymentHandlerContext paymentHandler, IpnHandlerFactory ipnHandlerFactory) {
        this.paymentHandler = paymentHandler;
        this.ipnHandlerFactory = ipnHandlerFactory;
    }


    @PostMapping
    public ResponseEntity<ResultObject> createPayment(@Valid @RequestBody PaymentRequest paymentRequest, HttpServletRequest httpServletRequest) {


        paymentRequest.setIpAddress(RequestUtil.getIpAddress(httpServletRequest));

        BasePaymentResponse paymentResponse = paymentHandler.executePayment(paymentRequest);

        ResultObject result = new ResultObject<>(true, "Create payment url successfully", HttpStatus.OK, paymentResponse);

        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    @GetMapping("/vnpay_ipn")
    IpnResponse processIpn(@RequestParam Map<String, String> params) {
        log.info("[VNPay Ipn] Params: {}", params);

        return ipnHandlerFactory.processIpn(PaymentMethod.VNPAY, params);
    }
    // Thêm cái chốt chặn này để hứng tin nhắn ngầm từ MoMo
    @PostMapping("/momo_ipn")
    public ResponseEntity<IpnResponse> processMoMoIpn(@RequestBody Map<String, String> params) {
        log.info("[MoMo Ipn] Params: {}", params);
        
        // Gọi nhà máy đẩy việc xử lý cho MoMoIpnHandler
        IpnResponse response = ipnHandlerFactory.processIpn(PaymentMethod.MOMO, params);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/vnpay_return")
public void vnpayReturn(
        @RequestParam Map<String,String> params,
        HttpServletResponse response
) throws IOException {

    log.info("Return params {}", params);

    String responseCode = params.get("vnp_ResponseCode");

    if ("00".equals(responseCode)) {

        response.sendRedirect("/pages/orders.html?status=success");

    } else {

        response.sendRedirect("/pages/orders.html?status=fail");

    }
}

}
