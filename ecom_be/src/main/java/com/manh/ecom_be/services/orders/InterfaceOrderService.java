package com.manh.ecom_be.services.orders;

import com.manh.ecom_be.dtos.OrderDTO;
import com.manh.ecom_be.models.Order;
import com.manh.ecom_be.responses.order.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InterfaceOrderService {
    Order createOrder(OrderDTO orderDTO) throws Exception;
    Order getOrderById(Long id);
    Order updateOrder(Long id, OrderDTO orderDTO) throws Exception;
    void deleteOrder(Long id);
    List<OrderResponse> findByUserId(Long userId);
    Page<Order> getOrdersByKeyword(String keyword, Pageable pageable);
}
