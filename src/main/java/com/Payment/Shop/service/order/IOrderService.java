package com.Payment.Shop.service.order;

import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.dto.request.CreateOrderRequest;
import com.Payment.Shop.dto.response.AdminOrderResponse;
import com.Payment.Shop.dto.response.BaseOrderResponse;
import com.Payment.Shop.entity.Order;

import java.util.List;

public interface IOrderService {

    // Existing
    BaseOrderResponse createOrder(CreateOrderRequest createOrderRequest);
    void markOrderAsPaid(Long orderId, PaymentMethod paymentMethod);
    Order findOrderWithItemsById(Long orderId);

    // New - User
    List<BaseOrderResponse> getMyOrders();
    BaseOrderResponse getOrderById(Long id);
    BaseOrderResponse cancelOrder(Long id);

    // New - Admin
    List<AdminOrderResponse> getAllOrders(String status);
    AdminOrderResponse updateOrderStatus(Long id, String status);

    // New - Payment
    void markOrderAsFailed(Long orderId, PaymentMethod paymentMethod);
}