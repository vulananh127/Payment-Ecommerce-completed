package com.Payment.Shop.service.payment.strategy;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.Payment.Shop.dto.request.PaymentRequest;
import com.Payment.Shop.dto.response.MoMoResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MoMoStrategy extends PaymentStrategy {

    // 1. Bê bộ Key Sandbox từ file PHP cũ sang
    private static final String PARTNER_CODE = "MOMOBKUN20180529";
    private static final String ACCESS_KEY = "klm05TvNBzhg7h7j";
    private static final String SECRET_KEY = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa";
    private static final String ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create";
    
    // Cấu hình link FE 
    private static final String RETURN_URL = "https://payment-ecommerce-completed.onrender.com/pages/orders.html";
    private static final String IPN_URL = "https://payment-ecommerce-completed.onrender.com/api/v1/payments/momo_ipn";

    @Override
    public MoMoResponse processPayment(PaymentRequest request) {
        try {
            // Lấy thông tin đơn hàng
            String orderId = request.getOrderId() + "_" + System.currentTimeMillis();
            String requestId = String.valueOf(System.currentTimeMillis());
            String amount = String.valueOf(request.getTotalAmount().longValue());
            String orderInfo = "Thanh toan don hang " + request.getOrderId() + " qua MoMo";
            String extraData = "";
            
            // 👉 ÉP LUỒNG THANH TOÁN ATM NỘI ĐỊA (Bê từ PHP)
            String requestType = "payWithATM"; 

            // 2. Tạo chuỗi rawHash theo đúng format của MoMo
            String rawHash = "accessKey=" + ACCESS_KEY +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + IPN_URL +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + PARTNER_CODE +
                    "&redirectUrl=" + RETURN_URL +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            // 3. Ký bảo mật (HMAC-SHA256)
            String signature = hmacSHA256(SECRET_KEY, rawHash);

            // 4. Đóng gói thành JSON
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", PARTNER_CODE);
            requestBody.put("partnerName", "Shop P&L");
            requestBody.put("storeId", "MomoTestStore");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", RETURN_URL);
            requestBody.put("ipnUrl", IPN_URL);
            requestBody.put("lang", "vi");
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType);
            requestBody.put("signature", signature);

            // 5. Bắn API sang MoMo
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.postForObject(ENDPOINT, entity, Map.class);

            // 6. Trả về Link cho Frontend chuyển hướng
            if (response != null && response.containsKey("payUrl")) {
                String payUrl = (String) response.get("payUrl");
                return MoMoResponse.builder().payUrl(payUrl).build();
            } else {
                log.error("MoMo API Error: {}", response);
                throw new RuntimeException("Không thể tạo link thanh toán MoMo");
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý MoMo: ", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    // Hàm tạo chữ ký HMAC-SHA256 (Tương đương hàm hash_hmac trong PHP)
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