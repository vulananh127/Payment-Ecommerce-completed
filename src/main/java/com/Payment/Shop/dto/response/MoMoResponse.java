package com.Payment.Shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MoMoResponse extends BasePaymentResponse {
    private String payUrl; // Cái link quét mã hoặc nhập thẻ ATM của MoMo
}