package com.Payment.Shop.service.payment.IpnHandler;

import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.dto.response.IpnResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IpnHandlerFactory {
    private final IpnHandler<IpnResponse> vnPayIpnHandler;

    public IpnHandlerFactory(IpnHandler<IpnResponse> vnPayIpnHandler ) {
        this.vnPayIpnHandler = vnPayIpnHandler;
    }

    public <T> IpnHandler<T> getIpnHandler(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case VNPAY -> (IpnHandler<T>) vnPayIpnHandler;
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
