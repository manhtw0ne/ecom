package com.manh.ecom_be.controllers;


import com.manh.ecom_be.components.SecurityUtils;
import com.manh.ecom_be.dtos.OrderDTO;
import com.manh.ecom_be.models.Order;
import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.responses.order.OrderResponse;
import com.manh.ecom_be.services.orders.InterfaceOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final InterfaceOrderService orderService;
    private final SecurityUtils securityUtils;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> createOrder(
            @Valid @RequestBody OrderDTO orderDTO, BindingResult result) throws Exception {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message(result.getFieldErrors().stream()
                            .map(FieldError::getDefaultMessage).collect(Collectors.joining(";")))
                    .status(HttpStatus.BAD_REQUEST).build());
        }

        if (orderDTO.getUserId() == null) {
            orderDTO.setUserId(securityUtils.getLoggedInUser().getId());
        }

        return ResponseEntity.ok(ResponseObject.builder()
                .message("Insert order successfully")
                .data(orderService.createOrder(orderDTO))
                .status(HttpStatus.OK).build());
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<ResponseObject> getOrdersByUser(
            @PathVariable("user_id") Long userId
    ) {
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Get orders successfully")
                .data(orderService.findByUserId(userId))
                .status(HttpStatus.OK).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Get order successfully")
                .data(OrderResponse.fromOrder(orderService.getOrderById(id)))
                .status(HttpStatus.OK).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> updateOrder(
            @PathVariable Long id, @RequestBody OrderDTO orderDTO
    ) throws Exception {
        return ResponseEntity.ok(ResponseObjetc.builder()
                .message("Update order successfully")
                .data(orderService.updateOrder(id, orderDTO))
                .status(HttpStatus.OK).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Delete order id " + id + " successfully")
                .status(HttpStatus.OK).build());
    }

    @GetMapping("")
    @PreAuthorize("hasRole("ROLE_ADMIN")")
    public ResponseEntity<ResponseObject> getAllOrders(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("orderDate").descending());
        Page<Order> orders = orderService.getOrdersByKeyword(keyword, pageRequest);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Get all orders successfully")
                .data(orders.getContent().stream().map(OrderResponse::fromOrder).toList())
                .status(HttpStatus.OK).build());
    }
}
