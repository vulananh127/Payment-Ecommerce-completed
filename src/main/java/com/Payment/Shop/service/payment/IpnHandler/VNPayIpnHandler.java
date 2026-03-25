package com.Payment.Shop.service.payment.IpnHandler;


import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.constant.VNPayParams;
import com.Payment.Shop.constant.VnpIpnResponseConst;
import com.Payment.Shop.dto.request.SavePaymentRequest;
import com.Payment.Shop.dto.response.IpnResponse;
import com.Payment.Shop.service.order.IOrderService;

import com.Payment.Shop.service.payment.strategy.VNPayStrategy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VNPayIpnHandler implements IpnHandler<IpnResponse> {

    private final VNPayStrategy vnPayStrategy;
    private final IOrderService orderService;

    private SavePaymentRequest buildSavePaymentRequest(Map<String, String> params) {
        var txnRef = params.get(VNPayParams.TXN_REF);
        // var orderId = Long.parseLong(txnRef);
        Long orderId = Long.parseLong(txnRef.split("_")[0]);

        SavePaymentRequest savePaymentRequest = new SavePaymentRequest();
        savePaymentRequest.setOrderId(orderId);
        savePaymentRequest.setPaymentMethod(PaymentMethod.VNPAY);
        savePaymentRequest.setTotalAmount(
            new BigDecimal(
                params.get(VNPayParams.AMOUNT)
            ).divide(BigDecimal.valueOf(100))
        );
                savePaymentRequest.setTransactionId(params.get(VNPayParams.TRANSACTION_NO));
        savePaymentRequest.setCreatedAt(params.get(VNPayParams.PAY_DATE));
        return savePaymentRequest;

    }

    // Process VNPay Ipn

    /**
     * Cơ chế retry IPN:
     * Hệ thống VNPAY căn cứ theo RspCode phản hồi từ merchant để kết thúc luồng hay bật cơ chế retry
     * RspCode: 00, 02 là mã lỗi IPN của merchant phản hồi đã cập nhật được tình trạng giao dịch. VNPAY kết thúc luồng
     * RspCode: 01, 04, 97, 99 hoặc IPN timeout là mã lỗi IPN merchant không cập nhật được tình trạng giao dịch. VNPAY bật cơ chế retry IPN
     * Tổng số lần gọi tối đa: 10 lần
     * Khoảng cách giữa các lần gọi lại: 5 phút
     *
     */
    
    @Override
    public IpnResponse processIPN(Map<String, String> params) {
    if (!vnPayStrategy.verifyIpn(params)) {
        return VnpIpnResponseConst.SIGNATURE_FAILED;
    }
    var txnRef = params.get(VNPayParams.TXN_REF);
    var responseCode = params.get(VNPayParams.RESPONSE_CODE);
    try {
        Long orderId = Long.parseLong(txnRef.split("_")[0]);
        
        if ("00".equals(responseCode)) {
            orderService.markOrderAsPaid(
                orderId,
                PaymentMethod.VNPAY
            );
            SavePaymentRequest req =
                buildSavePaymentRequest(params);
            vnPayStrategy.savePayment(req);
            return VnpIpnResponseConst.SUCCESS;
        } else {
            orderService.markOrderAsFailed(
                orderId,
                PaymentMethod.VNPAY
            );
            return VnpIpnResponseConst.FAILED;
        }

    } catch (Exception e) {

        return VnpIpnResponseConst.UNKNOWN_ERROR;
    }
}
}
