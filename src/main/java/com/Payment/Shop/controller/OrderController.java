package com.Payment.Shop.controller;

import com.Payment.Shop.dto.ResultObject;
import com.Payment.Shop.dto.request.CreateOrderRequest;
import com.Payment.Shop.dto.request.UpdateOrderStatusRequest;
import com.Payment.Shop.dto.response.AdminOrderResponse;
import com.Payment.Shop.dto.response.BaseOrderResponse;
import com.Payment.Shop.service.order.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    // USER - tạo đơn hàng
    @PostMapping("/api/v1/orders")
    public ResponseEntity<ResultObject> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        BaseOrderResponse order = orderService.createOrder(request);
        ResultObject<BaseOrderResponse> result = new ResultObject<>(
                true, "Create order successfully", HttpStatus.CREATED, order);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    // USER - xem đơn hàng của mình
    @GetMapping("/api/v1/orders")
    public ResponseEntity<ResultObject> getMyOrders() {
        List<BaseOrderResponse> orders = orderService.getMyOrders();
        ResultObject<List<BaseOrderResponse>> result = new ResultObject<>(
                true, "Get orders successfully", HttpStatus.OK, orders);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // USER - xem chi tiết đơn hàng
    @GetMapping("/api/v1/orders/{id}")
    public ResponseEntity<ResultObject> getOrderById(@PathVariable Long id) {
        BaseOrderResponse order = orderService.getOrderById(id);
        ResultObject<BaseOrderResponse> result = new ResultObject<>(
                true, "Get order successfully", HttpStatus.OK, order);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // USER - hủy đơn hàng
    @PutMapping("/api/v1/orders/{id}/cancel")
    public ResponseEntity<ResultObject> cancelOrder(@PathVariable Long id) {
        BaseOrderResponse order = orderService.cancelOrder(id);
        ResultObject<BaseOrderResponse> result = new ResultObject<>(
                true, "Cancel order successfully", HttpStatus.OK, order);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // ADMIN - xem tất cả đơn hàng
    @GetMapping("/api/v1/admin/orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP')")
    public ResponseEntity<ResultObject> getAllOrders(
            @RequestParam(required = false) String status) {
        List<AdminOrderResponse> orders = orderService.getAllOrders(status);
        ResultObject<List<AdminOrderResponse>> result = new ResultObject<>(
                true, "Get all orders successfully", HttpStatus.OK, orders);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // ADMIN - cập nhật trạng thái đơn hàng
    @PutMapping("/api/v1/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP')")
    public ResponseEntity<ResultObject> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        AdminOrderResponse order = orderService.updateOrderStatus(id, request.getStatus());
        ResultObject<AdminOrderResponse> result = new ResultObject<>(
                true, "Update order status successfully", HttpStatus.OK, order);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}