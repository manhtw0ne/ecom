package com.manh.ecom_be.repositories;

import com.manh.ecom_be.models.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderIdOrderByOrderIdDesc(Long orderId);
}
