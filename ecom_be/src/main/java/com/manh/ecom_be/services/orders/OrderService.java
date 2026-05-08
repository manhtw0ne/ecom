package com.manh.ecom_be.services.orders;


import com.manh.ecom_be.dtos.CartItemDTO;
import com.manh.ecom_be.dtos.OrderDTO;
import com.manh.ecom_be.models.*;
import com.manh.ecom_be.repositories.OrderDetailRepository;
import com.manh.ecom_be.repositories.OrderRepository;
import com.manh.ecom_be.repositories.ProductRepository;
import com.manh.ecom_be.repositories.UserRepository;
import com.manh.ecom_be.responses.order.OrderResponse;
import lombok.RequiredArgsConstructor;
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
public class OrderService implements InterfaceOrderService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Order createOrder(OrderDTO orderDTO) throws Exception {
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found: " + orderDTO.getUserId()));

        modelMapper.typeMap(OrderDTO, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        Order order = new Order();
        modelMapper.map(orderDTO, order);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setActive(true);

        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate()
                if (shippingDate.isBefore(LocalDate.now())) {
                    throw new DataNotFoundException("Shipping date must be at least today");
                };
                order.setShippingDate(shippingDate);

                if (orderDTO.getShippingAddress() == null) {
                    order.setShippingAddress(orderDTO.getAddress());
                }
                order.setShippingDate(shippingDate);

                if (orderDTO.getShippingAddress() == null) {
                    order.setShippingAddress(orderDTO.getAddress());
                }

                if (orderDTO.getVnpTxnRef() != null) {
                    order.setVnpTxnRef(orderDTO.getVnpTxnRef());
                }

                List<OrderDetail> orderDetails = new ArrayList<>();
                for (CartItemDTO item : orderDTO.getCartItems()) {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new DataNotFoundException("Product not found: " + item.getProductId()));

                    orderDetails.add(OrderDetail.builder()
                            .order(order)
                            .product(product)
                            .numberOfProducts(item.getQuantity())
                            .price(product.getPrice())
                            .totalMoney(product.getPrice() * item.getQuantity())
                            .build());
                }

                String couponCode = orderDTO.getCouponCode();
                if (couponCode != null && !couponCode.isEmpty()) {
                    Coupon coupon = couponRepository.findByCode(couponCode)
                            .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
                    if (!coupon.isActive()) {
                        throw new IllegalArgumentException("Coupon is not active");
                    }
                    order.setCoupon(coupon);
                }

                orderRepository.save(order);
                orderDetailRepository.saveAll(orderDetails);
                return order;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderDTO orderDTO) throws Exception {
        Order order = getOrderById(id);
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        modelMapper.map(orderDTO, order);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setActive(false);
            orderRepository.save(order);
        }
    }

    @Override
    public List<OrderResponse> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(OrderResponse::fromOrder).toList();
    }

    @Override
    public Page<Order> getOrdersByKeyword(String keyword, Pageable pageable) {
        return orderRepository.findByKeyword(keyword, pageable);
    }
}
