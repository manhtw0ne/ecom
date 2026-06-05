package com.manh.ecom_be.controllers;

import com.manh.ecom_be.components.LocalizationUtils;
import com.manh.ecom_be.dtos.OrderDetailDTO;
import com.manh.ecom_be.exceptions.DataNotFoundException;
import com.manh.ecom_be.models.OrderDetail;
import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.responses.order.OrderDetailResponse;
import com.manh.ecom_be.services.orderdetails.OrderDetailService;
import com.manh.ecom_be.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order-details")
@RequiredArgsConstructor

public class OrderDetailController {
    private final OrderDetailService orderDetailService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO) throws Exception {
        OrderDetail newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
        OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(newOrderDetail);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                .message("Create order detail successfully")
                .status(HttpStatus.CREATED)
                .data(orderDetailResponse)
                .build());

    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getOrderDetail(
            @Valid @PathVariable("id") Long id) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailService.getOrderDetail(id);
        OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(orderDetail);
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                .message("Get order detail successfully")
                .status(HttpStatus.OK)
                .data(orderDetailResponse)
                .build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ResponseObject> getOrderDetails(
            @Valid @PathVariable("orderId") Long orderId
    ) {
        List<OrderDetail> details = orderDetailService.findByOrderId(orderId);
        List<OrderDetailResponse> orderDetailResponses = details.stream()
                .map(OrderDetailResponse::fromOrderDetail)
                .toList();
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                .message("Get order details by orderId successfully")
                .status(HttpStatus.OK)
                .data(orderDetailResponses)
                .build());
    }

    @PutMapping("/{id}")
    @Operation(security = {@SecurityRequirement(name = "bearer-key")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO orderDetailDTO
    ) throws DataNotFoundException, Exception {
        OrderDetail orderDetail = orderDetailService.updateOrderDetail(id, orderDetailDTO);
        return ResponseEntity.ok(ResponseObject
                .builder()
                .data(orderDetail)
                .message("Update order detail successfully")
                .status(HttpStatus.OK)
                .build());
    }



//    @GetMapping("/order/{orderId}")
//    public ResponseEntity<ResponseObject> getOrderDetailsByOrder(
//            @Valid @PathVariable Long orderId
//    ) {
//        List<OrderDetailResponse> responses = orderDetailService.findByOrderId(orderId)
//                .stream().map(OrderDetailResponse::fromOrderDetail).toList();
//        return ResponseEntity.ok(ResponseObject.builder()
//                .message("Get order details by orderId successfully")
//                .status(HttpStatus.OK)
//                .data(responses)
//                .build());
//    }



    @DeleteMapping("/{id}")
    @Operation(security = {@SecurityRequirement(name = "bearer-key")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> deleteOrderDetail(
            @Valid @PathVariable("id") Long id) {
        orderDetailService.deleteById(id);
        return ResponseEntity.ok()
                .body(ResponseObject.builder()
                    .message(localizationUtils.getLocalizedMessage(
                            MessageKeys.DELETE_ORDER_DETAIL_SUCCESSFULLY)
                    ).build());
    }
}
