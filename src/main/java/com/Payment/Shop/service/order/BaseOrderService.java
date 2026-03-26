package com.Payment.Shop.service.order;

import com.Payment.Shop.constant.OrderStatus;
import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.constant.PaymentStatus;
import com.Payment.Shop.dto.request.CreateOrderRequest;
import com.Payment.Shop.dto.request.OrderItemRequest;
import com.Payment.Shop.dto.response.AdminOrderResponse;
import com.Payment.Shop.dto.response.BaseOrderResponse;
import com.Payment.Shop.dto.response.OrderItemResponse;
import com.Payment.Shop.dto.response.AdminOrderResponse;
import com.Payment.Shop.entity.Order;
import com.Payment.Shop.entity.OrderItem;
import com.Payment.Shop.entity.ProductVariant;
import com.Payment.Shop.entity.User;
import com.Payment.Shop.repository.OrderItemRepository;
import com.Payment.Shop.repository.OrderRepository;
import com.Payment.Shop.security.JwtUtil;
import com.Payment.Shop.service.payment.strategy.PaymentStrategy;
import com.Payment.Shop.service.product.IProductService;
import jakarta.persistence.OptimisticLockException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BaseOrderService implements IOrderService {
    private final ModelMapper modelMapper;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final IProductService productService;
//    private final PaymentHandlerContext paymentHandlerContext;


    public BaseOrderService(ModelMapper modelMapper, OrderRepository orderRepository, OrderItemRepository orderItemRepository, IProductService productService, PaymentStrategy paymentService) {
        this.modelMapper = modelMapper;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
    }

    @Override
    @Transactional
    public BaseOrderResponse createOrder(CreateOrderRequest createOrderRequest) {

        //LOG
        log.info("REQUEST productVariants = {}",
        createOrderRequest.getProductVariants());
        for (OrderItemRequest dto : createOrderRequest.getProductVariants()) {
    log.info("DTO variantId={}, qty={}",
            dto.getVariantId(),
            dto.getQuantity());
}

        String userId = JwtUtil.getCurrentUserLogin()
                        .orElseThrow(() -> new BadCredentialsException("User is not authenticated"));

        User curUser = new User();
        curUser.setId(Long.parseLong(userId));

        Map<Long, OrderItemRequest> variantDtoMap = createOrderRequest.getProductVariants().stream()
                .collect(Collectors.toMap(
                        productVariant -> productVariant.getVariantId(),
                        productVariant -> productVariant
                ));

        // Get product variants from database
        List<Long> productVariantIds = createOrderRequest.getProductVariants()
                .stream().map(productVariant ->
                        productVariant.getVariantId())
                .toList();

        List<ProductVariant> productVariants= productService.findAllProductVariantByVariantIdWithProduct(productVariantIds);
        productVariants.forEach(v ->
        log.info("DB variant id={}, price={}",
                v.getId(),
                v.getPrice())
);

        // Validate stock
        this.validateStock(productVariants, variantDtoMap);

        // Create order
        PaymentMethod method = createOrderRequest.getPaymentMethod();
        PaymentStatus paymentStatus;
        // tính totalAmount
        BigDecimal total = BigDecimal.ZERO;

        for (ProductVariant pv : productVariants) {
            OrderItemRequest dto =
                    variantDtoMap.get(pv.getId());
            if (dto == null) {
                throw new RuntimeException(
                        "Variant not found in request: " + pv.getId()
                );
            }

            BigDecimal price = pv.getPrice();

            BigDecimal qty =
                    BigDecimal.valueOf(dto.getQuantity());

            total = total.add(
                    price.multiply(qty)
            );
        }

        if (method == PaymentMethod.COD) {
            paymentStatus = PaymentStatus.UNPAID;
        } else {
            paymentStatus = PaymentStatus.PAYMENT_PROCESSING;
        }

        Order order = Order.builder()
                .address(createOrderRequest.getAddress())
                .note(createOrderRequest.getNote())
                .email(createOrderRequest.getEmail())
                .phone(createOrderRequest.getPhone())
                .receiverName(createOrderRequest.getReceiverName())
                .paymentMethod(method)
                .shippingFee(createOrderRequest.getShippingFee())
                .totalAmount(total)
                .user(curUser)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(paymentStatus)
                .build();


        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = productVariants.stream()
    .map(productVariant -> {

        OrderItemRequest dto = variantDtoMap.get(productVariant.getId());

        if (dto == null) {
            throw new RuntimeException("Variant mismatch: " + productVariant.getId());
        }

        Integer qty = dto.getQuantity();
        BigDecimal price = productVariant.getPrice();
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(qty));

        // ✅ Lấy productName và variantName (SKU)
        String productName = productVariant.getProduct() != null
                ? productVariant.getProduct().getName()
                : "Unknown";
        String variantName = productVariant.getSku();

        return OrderItem.builder()
                .order(savedOrder)
                .productVariant(new ProductVariant(productVariant.getId()))
                .quantity(qty)
                .unitPrice(price)
                .totalPrice(totalPrice)
                .productName(productName)   // ✅ THÊM
                .variantName(variantName)   // ✅ THÊM
                .build();
    })
    .toList();
        
        orderItemRepository.saveAll(orderItems);

        // Update stock
        this.updateStockOptimistic(productVariants, variantDtoMap);

//        this.updateStock(productVariants, variantDtoMap);

        return modelMapper.map(savedOrder, BaseOrderResponse.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOrderAsPaid(
            Long orderId,
            PaymentMethod method
    ) {

        Order order = orderRepository
            .findById(orderId)
            .orElseThrow();

        order.setPaymentStatus(
            PaymentStatus.PAYMENT_COMPLETED
        );

        orderRepository.save(order);
    }

    @Override
    public Order findOrderWithItemsById(Long orderId) {
        return orderRepository.findOrderByIdWithItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    }

    private void validateStock(List<ProductVariant> productVariants, Map<Long, OrderItemRequest> variantDtoMap) {

        for(ProductVariant productVariant : productVariants){
            OrderItemRequest dto =
        variantDtoMap.get(productVariant.getId());

Integer requestedQuantity =
        dto.getQuantity();
        
            if(productVariant.getStock() < requestedQuantity){
                log.error("Insufficient stock for variant ID: {}, available: {}, requested: {}",
                        productVariant.getId(), productVariant.getStock(), requestedQuantity);
                throw new IllegalArgumentException("Insufficient stock for product variant ID: " + productVariant.getId());
            }
        }
    }

    // private void updateStock( List<ProductVariant> productVariants, Map<Long, OrderItemRequest> variantDtoMap) {
    //     for(ProductVariant productVariant : productVariants){
    //         Integer requestedQuantity = variantDtoMap.get(productVariant.getId()).getQuantity();
    //         int newStock = productVariant.getStock() - requestedQuantity;

    //         productVariant.setStock(newStock);
    //         log.info("Updated stock for variant ID: {}, new stock: {}", productVariant.getId(), newStock);
    //     }

    //     productService.saveAllProductVariant(productVariants);
    // }

    private void restock(Order order) {
        // ❌ tránh restock 2 lần
    if (order.isRestocked()) {
        log.warn("Order {} already restocked, skip", order.getId());
        return;
    }

    for (OrderItem item : order.getOrderItems()) {
        Long variantId = item.getProductVariant().getId();
        int qty = item.getQuantity();

        productService.increaseStock(variantId, qty);

        log.info("Restocked variantId={}, qty={}", variantId, qty);
    }

    order.setRestocked(true);
}

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {

        return switch (current) {
            case PENDING ->
                next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELED;

            case CONFIRMED ->
                next == OrderStatus.ON_DELIVERY || next == OrderStatus.CANCELED;

            case ON_DELIVERY ->
                next == OrderStatus.DELIVERED;

            default -> false;
        };
    }
    // Optimistic lock
    private void updateStockOptimistic( List<ProductVariant> productVariants, Map<Long, OrderItemRequest> variantDtoMap) {
        for(ProductVariant productVariant : productVariants){
            OrderItemRequest dto =
            variantDtoMap.get(productVariant.getId());

    Integer requestedQuantity =
            dto.getQuantity();

            int updatedRows = productService.updateStockOptimistic(productVariant.getId(), requestedQuantity);


            if (updatedRows == 0) {
                // If no rows were updated, it means the condition failed (stock changed)
                log.error("Optimistic lock failure: The product stock for variant ID {} was modified by another transaction.",
                        productVariant.getId());
                throw new OptimisticLockException("Out of stock. Please try again.");
            }

            log.info("Updated stock for variant ID: {}, new stock: {}", productVariant.getId(), productVariant.getStock() - requestedQuantity);
        }


    }

    // Mới
    @Override
    public List<BaseOrderResponse> getMyOrders() {
        String userId = JwtUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadCredentialsException("User is not authenticated"));

        List<Order> orders = orderRepository.findByUserIdWithItems(Long.parseLong(userId));

        return orders.stream().map(order -> {

            BaseOrderResponse res = modelMapper.map(order, BaseOrderResponse.class);

            List<OrderItemResponse> items = order.getOrderItems().stream().map(item -> {

                OrderItemResponse dto = modelMapper.map(item, OrderItemResponse.class);

                // ✅ SET IMAGE URL
                if (item.getProductVariant() != null &&
                    item.getProductVariant().getProduct() != null) {

                    dto.setImageUrl(item.getProductVariant().getProduct().getImageUrl());
                }

                return dto;
            }).collect(Collectors.toList());

            res.setOrderItems(items);

            return res;

        }).collect(Collectors.toList());
    }

    @Override
    public BaseOrderResponse getOrderById(Long id) {
        Order order = orderRepository.findOrderByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        BaseOrderResponse res = modelMapper.map(order, BaseOrderResponse.class);

        List<OrderItemResponse> items = order.getOrderItems().stream().map(item -> {

            OrderItemResponse dto = modelMapper.map(item, OrderItemResponse.class);

            if (item.getProductVariant() != null &&
                item.getProductVariant().getProduct() != null) {

                dto.setImageUrl(item.getProductVariant().getProduct().getImageUrl());
            }

            return dto;
        }).collect(Collectors.toList());

        res.setOrderItems(items);

        return res;
    }

 
    @Override
    @Transactional
    public BaseOrderResponse cancelOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new IllegalArgumentException("Chỉ được hủy đơn ở trạng thái PENDING");
        }

        // ✅ RESTOCK
        restock(order);

        order.setOrderStatus(OrderStatus.CANCELED);

        return modelMapper.map(orderRepository.save(order), BaseOrderResponse.class);
    }
 
    @Override
    public List<AdminOrderResponse> getAllOrders(String status) {
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByOrderStatus(OrderStatus.valueOf(status));
        } else {
            orders = orderRepository.findAll();
        }
        return orders.stream()
                .map(o -> modelMapper.map(o, AdminOrderResponse.class))
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional
    public AdminOrderResponse updateOrderStatus(Long id, String status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        OrderStatus current = order.getOrderStatus();
        OrderStatus next = OrderStatus.valueOf(status);

        // ❌ Không cho update nếu đã CANCEL hoặc DELIVERED
        if (current == OrderStatus.CANCELED || current == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Không thể cập nhật đơn đã hoàn thành hoặc đã hủy");
        }

        // ✅ Validate flow
        if (!isValidTransition(current, next)) {
            throw new IllegalArgumentException("Sai thứ tự trạng thái");
        }

        // ✅ Nếu chuyển sang CANCEL → restock
        if (next == OrderStatus.CANCELED) {
            restock(order);
        }

        order.setOrderStatus(next);

        return modelMapper.map(orderRepository.save(order), AdminOrderResponse.class);
    }
 
    @Override
    @Transactional
    public void markOrderAsFailed(
            Long orderId,
            PaymentMethod method
    ) {
        Order order = orderRepository
            .findById(orderId)
            .orElseThrow();
        
        // ✅ tránh double
        if (order.getOrderStatus() != OrderStatus.CANCELED) {
            restock(order);
        }

        order.setPaymentStatus( PaymentStatus.PAYMENT_FAILED);
        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
}
}
