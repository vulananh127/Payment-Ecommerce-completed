package com.Payment.Shop.service.payment.IpnHandler;

import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.dto.response.IpnResponse;

import org.hibernate.query.sqm.mutation.internal.InsertHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IpnHandlerFactory {
    private final IpnHandler<IpnResponse> vnPayIpnHandler;
    private final IpnHandler<IpnResponse> moMoIpnHandler; // THÊM DÒNG NÀY

    public IpnHandlerFactory(
        @org.springframework.beans.factory.annotation.Qualifier("VNPayIpnHandler") IpnHandler<IpnResponse> vnPayIpnHandler,
        @org.springframework.beans.factory.annotation.Qualifier("moMoIpnHandler") IpnHandler<IpnResponse> moMoIpnHandler
    ) {
        this.vnPayIpnHandler = vnPayIpnHandler;
        this.moMoIpnHandler = moMoIpnHandler;
    }

    public <T> IpnHandler<T> getIpnHandler(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case VNPAY -> (IpnHandler<T>) vnPayIpnHandler;
            case MOMO -> (IpnHandler<T>) moMoIpnHandler; // THÊM DÒNG NÀY
            default -> throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        };
    }


    public <T> T processIpn(PaymentMethod method, Map<String, String> params) {
        IpnHandler<T> handler = getIpnHandler(method);
        return handler.processIPN(params);
    }

    public IpnHandler<IpnResponse> getVnPayIpnHandler() {
        return vnPayIpnHandler;
    }
}
