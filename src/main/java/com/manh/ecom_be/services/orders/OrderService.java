package com.manh.ecom_be.services.orders;


import com.manh.ecom_be.dtos.CartItemDTO;
import com.manh.ecom_be.dtos.OrderDTO;
import com.manh.ecom_be.dtos.OrderDetailDTO;
import com.manh.ecom_be.dtos.OrderWithDetailsDTO;
import com.manh.ecom_be.exceptions.DataNotFoundException;
import com.manh.ecom_be.models.*;
import com.manh.ecom_be.repositories.CouponRepository;
import com.manh.ecom_be.repositories.OrderDetailRepository;
import com.manh.ecom_be.repositories.OrderRepository;
import com.manh.ecom_be.repositories.ProductRepository;
import com.manh.ecom_be.repositories.UserRepository;
import com.manh.ecom_be.responses.order.OrderResponse;
import com.manh.ecom_be.components.metrics.BusinessMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements InterfaceOrderService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final OrderDetailRepository orderDetailRepository;

    private final ModelMapper modelMapper;
    private final BusinessMetrics businessMetrics;

    @Override
    @Transactional
    public Order createOrder(OrderDTO orderDTO) throws Exception {
        User user = userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found: " + orderDTO.getUserId()));

        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        Order order = new Order();
        modelMapper.map(orderDTO, order);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        LocalDate shippingDate = orderDTO.getShippingDate() == null
                ? LocalDate.now() : orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())) {
            throw new DataNotFoundException("Date must be at least today");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);

        order.setTotalMoney(orderDTO.getTotalMoney());

        if (orderDTO.getVnpTxnRef() != null) {
            order.setVnpTxnRef(orderDTO.getVnpTxnRef());
        }

        if (orderDTO.getShippingAddress() == null) {
            order.setShippingAddress(orderDTO.getAddress());
        }

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItemDTO cartItemDTO : orderDTO.getCartItems()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);

            Long productId = cartItemDTO.getProductId();
            int quantity = cartItemDTO.getQuantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + productId));

            orderDetail.setProduct(product);
            orderDetail.setNumberOfProducts(quantity);
            orderDetail.setPrice(product.getPrice());
            orderDetails.add(orderDetail);
        }

        String couponCode = orderDTO.getCouponCode();
        if (!couponCode.isEmpty()) {
            Coupon coupon = couponRepository.findByCode(couponCode)
                    .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
            if (!coupon.isActive()) {
                throw new IllegalArgumentException("Coupon is not active");
            }
            order.setCoupon(coupon);
        } else {
            order.setCoupon(null);
        }

        orderDetailRepository.saveAll(orderDetails);
        orderRepository.save(order);
        businessMetrics.incrementOrdersCreated();
        log.info("Order created successfully: orderId={}, userId={}", order.getId(), order.getUser().getId());
        return order;
    }

    @Transactional
    public Order updateOrderWithDetails(OrderWithDetailsDTO orderWithDetailsDTO) {
        modelMapper.typeMap(OrderWithDetailsDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        Order order = new Order();
        modelMapper.map(orderWithDetailsDTO, order);
        Order savedOrder = orderRepository.save(order);

        for (OrderDetailDTO orderDetailDTO : orderWithDetailsDTO.getOrderDetailDTOS()) {

        }

        List<OrderDetail> savedOrderDetails = orderDetailRepository.saveAll(order.getOrderDetails());
        savedOrder.setOrderDetails(savedOrderDetails);
        return savedOrder;
    }



    @Override
    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            order = orderRepository.findByVnpTxnRef(orderId.toString()).orElse(null);
        }
        return order;
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {
        Order order = getOrderById(id);
        User existingUser = userRepository.findById(
                orderDTO.getUserId()).orElseThrow(() ->
                new DataNotFoundException("Cannot find user with id: " + id));

        if (orderDTO.getUserId() != null) {
            User user = new User();
            user.setId(orderDTO.getUserId());
            order.setUser(user);
        }

        if (orderDTO.getFullName() != null && !orderDTO.getFullName().trim().isEmpty()) {
            order.setFullName(orderDTO.getFullName().trim());
        }

        if (orderDTO.getEmail() != null && !orderDTO.getEmail().trim().isEmpty()) {
            order.setEmail(orderDTO.getEmail().trim());
        }

        if (orderDTO.getPhoneNumber() != null && !orderDTO.getPhoneNumber().trim().isEmpty()) {
            order.setPhoneNumber(orderDTO.getPhoneNumber().trim());
        }

        if (orderDTO.getStatus() != null && !orderDTO.getStatus().trim().isEmpty()) {
            order.setStatus(orderDTO.getStatus().trim());
        }

        if (orderDTO.getAddress() != null && !orderDTO.getAddress().trim().isEmpty()) {
            order.setAddress(orderDTO.getAddress().trim());
        }

        if (orderDTO.getNote() != null && !orderDTO.getNote().trim().isEmpty()) {
            order.setNote(orderDTO.getNote().trim());
        }

        if (orderDTO.getTotalMoney() != null) {
            order.setTotalMoney(orderDTO.getTotalMoney());
        }

        if (orderDTO.getShippingMethod() != null && !orderDTO.getShippingMethod().trim().isEmpty()) {
            order.setShippingMethod(orderDTO.getShippingMethod().trim());
        }

        if (orderDTO.getShippingAddress() != null && !orderDTO.getShippingAddress().trim().isEmpty()) {
            order.setShippingAddress(orderDTO.getShippingAddress().trim());
        }

        if (orderDTO.getShippingDate() != null) {
            order.setShippingDate(orderDTO.getShippingDate());
        }

        if (orderDTO.getPaymentMethod() != null && !orderDTO.getPaymentMethod().trim().isEmpty()) {
            order.setPaymentMethod(orderDTO.getPaymentMethod().trim());
        }

        order.setUser(existingUser);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = getOrderById(orderId);

        if (order != null) {
            order.setActive(false);
            orderRepository.save(order);
        }
    }

    @Override
    public List<OrderResponse> findByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream().map(order -> OrderResponse.fromOrder(order)).toList();
    }

    @Override
    public Page<Order> getOrdersByKeyword(String keyword, Pageable pageable) {
        return orderRepository.findByKeyword(keyword, pageable);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, String status) throws DataNotFoundException, IllegalArgumentException {
        Order order = getOrderById(id);
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        if (!OrderStatus.VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status " + status);
        }

        String currentStatus = order.getStatus();
        if (currentStatus.equals(OrderStatus.DELIVERED) && !status.equals(OrderStatus.CANCELLED)) {
            throw new IllegalArgumentException("Cannot change status of a DELIVERED order");
        }

        if (status.equals(OrderStatus.CANCELLED)) {
            throw new IllegalArgumentException("Cannot change status of a CANCELLED order");
        }

        if (status.equals(OrderStatus.CANCELLED)) {
            if (!currentStatus.equals(OrderStatus.PENDING)) {
                throw new IllegalArgumentException("Cannot change status of a PENDING order");
            }
        }

        order.setStatus(status);
        if (OrderStatus.CANCELLED.equals(status)) {
            businessMetrics.incrementOrdersCancelled();
            log.info("Order cancelled: orderId={}", order.getId());
        }

        return orderRepository.save(order);
    }
}
