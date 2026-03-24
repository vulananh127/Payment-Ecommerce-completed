package com.Payment.Shop.service.order;

import com.Payment.Shop.constant.OrderStatus;
import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.constant.PaymentStatus;
import com.Payment.Shop.dto.request.CreateOrderRequest;
import com.Payment.Shop.dto.request.ProductVariantDto;
import com.Payment.Shop.dto.response.BaseOrderResponse;
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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

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

        String userId = JwtUtil.getCurrentUserLogin()
                        .orElseThrow(() -> new BadCredentialsException("User is not authenticated"));

        User curUser = new User();
        curUser.setId(Long.parseLong(userId));

        Map<Long, ProductVariantDto> variantDtoMap = createOrderRequest.getProductVariants().stream()
                .collect(Collectors.toMap(
                        productVariant -> productVariant.getVariantId(),
                        productVariant -> productVariant
                ));

        // Get product variants from database
        List<Long> productVariantIds = createOrderRequest.getProductVariants()
                .stream().map(productVariant ->
                        productVariant.getVariantId())
                .toList();

        List<ProductVariant> productVariants= productService.findAllProductVariantByVariantId(productVariantIds);

        // Load product variants in and lock using Pessimistic lock
//        List<ProductVariant> productVariants= productService.findAllProductVariantByVariantIdWithLock(productVariantIds);

        // Validate stock
        this.validateStock(productVariants, variantDtoMap);


        // Create order
        Order order = Order.builder()
                .address(createOrderRequest.getAddress())
                .note(createOrderRequest.getNote())
                .email(createOrderRequest.getEmail()) // thêm email
                .phone(createOrderRequest.getPhone())
                .receiverName(createOrderRequest.getReceiverName())
                .paymentMethod(createOrderRequest.getPaymentMethod())
                .shippingFee(createOrderRequest.getShippingFee())
                .totalAmount(createOrderRequest.getTotalAmount())
                .user(curUser)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PAYMENT_PROCESSING)
                .build();



//        productVariants.forEach(productVariant -> {
//            OrderItem orderItem = OrderItem.builder()
//                    .order(order)
//                    .productVariant(new ProductVariant(productVariant.getId()))
//                    .quantity(variantDtoMap.get(productVariant.getId()).getQuantity())
//                    .totalPrice(productVariant.getPrice() * variantDtoMap.get(productVariant.getId()).getQuantity())
//                    .build();
//
//            order.addOrderItem(orderItem);
//        });


        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = productVariants.stream().map(productVariant -> {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productVariant(new ProductVariant(productVariant.getId()))
                    .quantity(variantDtoMap.get(productVariant.getId()).getQuantity())
                    .totalPrice(productVariant.getPrice() * variantDtoMap.get(productVariant.getId()).getQuantity())
                    .build();
            return orderItem;
        }).toList();

        orderItemRepository.saveAll(orderItems);

//        if(!savedOrder.getPaymentMethod().equals(PaymentMethod.CoD)){
//            // Process payment
//            BasePaymentResponse paymentResponse = processPayment(savedOrder);
//            order.setPaymentStatus(PaymentStatus.PAYMENT_COMPLETED);
//            orderRepository.save(order);
//        }

        // Update stock
        this.updateStockOptimistic(productVariants, variantDtoMap);

//        this.updateStock(productVariants, variantDtoMap);

        return modelMapper.map(savedOrder, BaseOrderResponse.class);
    }

    @Override
    public void markOrderAsPaid(Long orderId, PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId).
                orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setPaymentStatus(PaymentStatus.PAYMENT_COMPLETED);
        order.setOrderStatus(OrderStatus.PAID);
        order.setPaymentMethod(paymentMethod);
        orderRepository.save(order);
    }

    @Override
    public Order findOrderWithItemsById(Long orderId) {
        return orderRepository.findOrderByIdWithItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    }

    private void validateStock(List<ProductVariant> productVariants, Map<Long, ProductVariantDto> variantDtoMap) {

        for(ProductVariant productVariant : productVariants){
            Integer requestedQuantity = variantDtoMap.get(productVariant.getId()).getQuantity();
            if(productVariant.getStock() < requestedQuantity){
                log.error("Insufficient stock for variant ID: {}, available: {}, requested: {}",
                        productVariant.getId(), productVariant.getStock(), requestedQuantity);
                throw new IllegalArgumentException("Insufficient stock for product variant ID: " + productVariant.getId());
            }
        }
    }
//    private BasePaymentResponse processPayment(Order order){
//
//        PaymentRequest paymentRequest = PaymentRequest.builder()
//                .orderId(order.getId())
//                .userId(order.getUser().getId())
//                .totalAmount(order.getTotalAmount())
//                .paymentMethod(order.getPaymentMethod())
//                .build();
//
//        BasePaymentResponse paymentResponse = paymentHandlerContext.executePayment(paymentRequest);
//
//        return paymentResponse;
//    }

    private void updateStock( List<ProductVariant> productVariants, Map<Long, ProductVariantDto> variantDtoMap) {
        for(ProductVariant productVariant : productVariants){
            Integer requestedQuantity = variantDtoMap.get(productVariant.getId()).getQuantity();
            int newStock = productVariant.getStock() - requestedQuantity;

            productVariant.setStock(newStock);
            log.info("Updated stock for variant ID: {}, new stock: {}", productVariant.getId(), newStock);
        }

        productService.saveAllProductVariant(productVariants);
    }

    // Optimistic lock
    private void updateStockOptimistic( List<ProductVariant> productVariants, Map<Long, ProductVariantDto> variantDtoMap) {
        for(ProductVariant productVariant : productVariants){
            Integer requestedQuantity = variantDtoMap.get(productVariant.getId()).getQuantity();

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
 
        List<Order> orders = orderRepository.findByUserId(Long.parseLong(userId));
        return orders.stream()
                .map(o -> modelMapper.map(o, BaseOrderResponse.class))
                .collect(Collectors.toList());
    }
 
    @Override
    public BaseOrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        return modelMapper.map(order, BaseOrderResponse.class);
    }
 
    @Override
    @Transactional
    public BaseOrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
 
        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new IllegalArgumentException("Cannot cancel order with status: " + order.getOrderStatus());
        }
 
        order.setOrderStatus(OrderStatus.CANCELED);
        Order saved = orderRepository.save(order);
        return modelMapper.map(saved, BaseOrderResponse.class);
    }
 
    @Override
    public List<BaseOrderResponse> getAllOrders(String status) {
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByOrderStatus(OrderStatus.valueOf(status));
        } else {
            orders = orderRepository.findAll();
        }
        return orders.stream()
                .map(o -> modelMapper.map(o, BaseOrderResponse.class))
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional
    public BaseOrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        order.setOrderStatus(OrderStatus.valueOf(status));
        Order saved = orderRepository.save(order);
        return modelMapper.map(saved, BaseOrderResponse.class);
    }
 
    @Override
    @Transactional
    public void markOrderAsFailed(Long orderId, PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        // Giữ PENDING để user có thể thanh toán lại
        order.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
        orderRepository.save(order);
    }
}
