package com.Payment.Shop.service.payment.strategy;

import com.Payment.Shop.config.PaymentConfig.VNPayConfig;
import com.Payment.Shop.constant.Symbol;
import com.Payment.Shop.constant.VNPayParams;
import com.Payment.Shop.dto.request.PaymentRequest;

import com.Payment.Shop.dto.response.VNPayResponse;
import com.Payment.Shop.util.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
@Primary
public class VNPayStrategy extends PaymentStrategy {

    private final VNPayConfig vnPayConfig;

    public VNPayStrategy(VNPayConfig vnPayConfig) {
        super();
        this.vnPayConfig = vnPayConfig;
    }


    @Override
    public VNPayResponse processPayment(PaymentRequest request) {

        Long amount = (long) (request.getTotalAmount() * VNPayConfig.DEFAULT_MULTIPLIER);  // 1. amount * 100
        var txnRef = String.valueOf(request.getOrderId());                       // 2. orderId
        var returnUrl = buildReturnUrl(txnRef);                 // 3. FE redirect by returnUrl

        var ipAddress = request.getIpAddress();
        var orderInfo = buildPaymentDetail(request);

        Map<String, String> params = vnPayConfig.getVNPayConfig();

        params.put(VNPayParams.TXN_REF, txnRef);
        params.put(VNPayParams.RETURN_URL, returnUrl);
        // params.put("vnp_IpnUrl", vnPayConfig.getIpnUrl());

        params.put(VNPayParams.IP_ADDRESS, ipAddress);
        params.put(VNPayParams.ORDER_INFO, orderInfo);
        params.put(VNPayParams.AMOUNT, String.valueOf(amount));

        var initPaymentUrl = buildInitPaymentUrl(params);

        return VNPayResponse.builder()
                .vnpUrl(initPaymentUrl)
                .build();
    }

    private String buildInitPaymentUrl(Map<String, String> params) {
        var hashPayload = new StringBuilder();
        var query = new StringBuilder();
        var fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);   // 1. Sort field names

        var itr = fieldNames.iterator();
        while (itr.hasNext()) {
            var fieldName = itr.next();
            var fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // 2.1. Build hash data
                hashPayload.append(fieldName);
                hashPayload.append(Symbol.EQUAL);
                hashPayload.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                // 2.2. Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append(Symbol.EQUAL);
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                if (itr.hasNext()) {
                    query.append(Symbol.AND);
                    hashPayload.append(Symbol.AND);
                }
            }
        }

        // 3. Build secureHash
        var secureHash = CryptoUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashPayload.toString());

        // 4. Finalize query
        query.append("&vnp_SecureHash=");
        query.append(secureHash);

        return vnPayConfig.getInitPaymentPrefixUrl() + "?" + query;
    }

    private String buildPaymentDetail(PaymentRequest request) {
        var orderInfo = request.getOrderId();

        return String.format("Thanh toan don hang %s. So tien %s", orderInfo, request.getTotalAmount());
    }

    private String buildReturnUrl(String orderId) {
    return vnPayConfig.getReturnUrlFormat();
}
    

    public boolean verifyIpn(Map<String, String> params) {
    //     var reqSecureHash = params.get(VNPayParams.SECURE_HASH);
    //     params.remove(VNPayParams.SECURE_HASH);
    //     params.remove(VNPayParams.SECURE_HASH_TYPE);
    //     var hashPayload = new StringBuilder();
    //     var fieldNames = new ArrayList<>(params.keySet());
    //     Collections.sort(fieldNames);

    //     var itr = fieldNames.iterator();
    //     while (itr.hasNext()) {
    //         var fieldName = itr.next();
    //         var fieldValue = params.get(fieldName);
    //         if ((fieldValue != null) && (!fieldValue.isEmpty())) {
    //             //Build hash data
    //             hashPayload.append(fieldName);
    //             hashPayload.append(Symbol.EQUAL);
    //             hashPayload.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

    //             if (itr.hasNext()) {
    //                 hashPayload.append(Symbol.AND);
    //             }
    //         }
    //     }

    //     var secureHash = CryptoUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashPayload.toString());
    //     return secureHash.equals(reqSecureHash);

            var reqSecureHash = params.get(VNPayParams.SECURE_HASH);
            params.remove(VNPayParams.SECURE_HASH);
            params.remove(VNPayParams.SECURE_HASH_TYPE);

            var fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            var hashPayload = new StringBuilder();
            for (var fieldName : fieldNames) {
                var fieldValue = params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    if (hashPayload.length() > 0) hashPayload.append("&");
                    hashPayload.append(fieldName)
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                }
            }

            var secureHash = CryptoUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashPayload.toString());
            return secureHash.equalsIgnoreCase(reqSecureHash);
        }
    

}
