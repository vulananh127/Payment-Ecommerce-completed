package com.Payment.Shop.repository;

import com.Payment.Shop.constant.OrderStatus;
import com.Payment.Shop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Giữ nguyên method có sẵn
    @Query("SELECT o FROM Order o LEFT JOIN " +
            "FETCH o.orderItems i LEFT JOIN FETCH i.productVariant v " +
            "JOIN FETCH v.product " +
            "WHERE o.id = :orderId")
    Optional<Order> findOrderByIdWithItems(@Param("orderId") Long orderId);

    // Thêm mới - lấy đơn hàng của 1 user
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserId(@Param("userId") Long userId);

    // Thêm mới - lọc theo status
    List<Order> findByOrderStatus(OrderStatus status);

    // Lấy tất cả có orderItems
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems ORDER BY o.createdAt DESC")
    List<Order> findAllWithItems();
}