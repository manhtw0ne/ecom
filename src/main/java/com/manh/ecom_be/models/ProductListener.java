package com.manh.ecom_be.models;


import com.manh.ecom_be.services.product.InterfaceProductRedisService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@AllArgsConstructor
public class ProductListener {

    private final InterfaceProductRedisService productRedisService;
    private static final Logger logger = LoggerFactory.getLogger(ProductListener.class);

    @PrePersist
    public void prePersist(Product product) {
        logger.info("perPersist");
    }

    @PostPersist
    public void postPersist(Product product) {
        logger.info("postPersist - clearing Redis cache");
        productRedisService.clear();
    }

    @PreUpdate
    public void preUpdate(Product product) {
        logger.info("preUpdate");
    }

    @PostUpdate
    public void postUpdate(Product product) {
        logger.info("postUpdate");
        productRedisService.clear();
    }

    @PreRemove
    public void preRemove(Product product) {
        logger.info("preRemove");
    }


    @PostRemove
    public void postRemove(Product product) {
        logger.info("postRemove - clearing Redis cache");
        productRedisService.clear();
    }
}
