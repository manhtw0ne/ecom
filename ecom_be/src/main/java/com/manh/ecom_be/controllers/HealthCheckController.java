package com.manh.ecom_be.controllers;


import com.manh.ecom_be.models.Category;
import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.services.category.InterfaceCategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/healthcheck")
@AllArgsConstructor
public class HealthCheckController {
    private final InterfaceCategoryService categoryService;

    @GetMapping("/health")
    public ResponseEntity<ResponseObject> healthCheck() throws Exception {
        List<Category> categories = categoryService.getAllCategories();

        String computerName = InetAddress.getLocalHost().getHostName();

        return ResponseEntity.ok(ResponseObject
                .builder()
                .message("ok, Computer Name: " + computerName)
                .status(HttpStatus.OK)
                .build());
    }
}
