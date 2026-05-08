package com.manh.ecom_be.models;


import com.manh.ecom_be.services.product.InterfaceProductRedisService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductListener {
    private InterfaceProductRedisService productRedisService;

    @Autowired
    public void setProductRedisService(InterfaceProductRedisService productRedisService) {
        this.productRedisService = productRedisService;
    }

    @PrePersist
    public void prePersist(Product product) {}

    @PostPersist
    public void postPersist(Product product) {
        productRedisService.clear();
    }

    @PostUpdate
    public void postUpdate(Product product) {
        productRedisService.clear();
    }

    @PostRemove
    public void postRemove(Product product) {
        productRedisService.clear();
    }
}
