package com.Payment.Shop.service.payment.IpnHandler;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.constant.VnpIpnResponseConst;
import com.Payment.Shop.dto.request.SavePaymentRequest;
import com.Payment.Shop.dto.response.IpnResponse;
import com.Payment.Shop.service.order.IOrderService;
import com.Payment.Shop.service.payment.strategy.MoMoStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoMoIpnHandler implements IpnHandler<IpnResponse> {

    private final IOrderService orderService;
    private final MoMoStrategy moMoStrategy; // Dùng lại để lưu lịch sử giao dịch

    
    private static final String ACCESS_KEY = "klm05TvNBzhg7h7j";
    private static final String SECRET_KEY = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa";

    @Override
    public IpnResponse processIPN(Map<String, String> params) {
        log.info("MoMo IPN Received: {}", params);
        try {
            // 1. Lấy chữ ký do MoMo gửi về
            String reqSignature = params.get("signature");

            // 2. Tạo chuỗi ký theo đúng chuẩn API MoMo IPN
            String rawHash = "accessKey=" + ACCESS_KEY +
                    "&amount=" + params.getOrDefault("amount", "") +
                    "&extraData=" + params.getOrDefault("extraData", "") +
                    "&message=" + params.getOrDefault("message", "") +
                    "&orderId=" + params.getOrDefault("orderId", "") +
                    "&orderInfo=" + params.getOrDefault("orderInfo", "") +
                    "&orderType=" + params.getOrDefault("orderType", "") +
                    "&partnerCode=" + params.getOrDefault("partnerCode", "") +
                    "&payType=" + params.getOrDefault("payType", "") +
                    "&requestId=" + params.getOrDefault("requestId", "") +
                    "&responseTime=" + params.getOrDefault("responseTime", "") +
                    "&resultCode=" + params.getOrDefault("resultCode", "") +
                    "&transId=" + params.getOrDefault("transId", "");

            // 3. Băm chữ ký để đối chiếu (Chống hacker)
            String mySignature = hmacSHA256(SECRET_KEY, rawHash);
            if (!mySignature.equals(reqSignature)) {
                log.error("Lỗi: Chữ ký MoMo IPN không khớp!");
                return VnpIpnResponseConst.SIGNATURE_FAILED; // Tái sử dụng hằng số có sẵn
            }

            // 4. Tách lấy OrderId thật (Bỏ đuôi timestamp đã nối lúc tạo link)
            String orderIdStr = params.get("orderId");
            Long orderId = Long.parseLong(orderIdStr.split("_")[0]);

            // 5. Kiểm tra trạng thái giao dịch (resultCode = 0 là thành công)
            String resultCode = String.valueOf(params.get("resultCode"));

            if ("0".equals(resultCode)) {
                // Đổi trạng thái đơn hàng sang ĐÃ THANH TOÁN
                orderService.markOrderAsPaid(orderId, PaymentMethod.MOMO);

                // Lưu lịch sử giao dịch vào bảng Payment
                SavePaymentRequest req = new SavePaymentRequest();
                req.setOrderId(orderId);
                req.setPaymentMethod(PaymentMethod.MOMO);
                req.setTotalAmount(new BigDecimal(params.get("amount")));
                req.setTransactionId(params.get("transId"));
                
                moMoStrategy.savePayment(req);

                return VnpIpnResponseConst.SUCCESS;
            } else {
                // Thất bại hoặc khách hủy giao dịch
                orderService.markOrderAsFailed(orderId, PaymentMethod.MOMO);
                return VnpIpnResponseConst.FAILED;
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý MoMo IPN: ", e);
            return VnpIpnResponseConst.UNKNOWN_ERROR;
        }
    }

    // Hàm tạo chữ ký HMAC-SHA256
    private String hmacSHA256(String secretKey, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] bytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hash.append('0');
            }
            hash.append(hex);
        }
        return hash.toString();
    }
}