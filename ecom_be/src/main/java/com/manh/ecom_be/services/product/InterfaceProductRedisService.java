package com.manh.ecom_be.services.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.manh.ecom_be.responses.ProductResponse;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface InterfaceProductRedisService {
    List<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) throws JsonProcessingException;
    void saveAllProducts(List<ProductResponse> products, String keyword, Long categoryId, PageRequest pageRequest) throws JsonProcessingException;
    void clear();
}
