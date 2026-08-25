package com.manh.ecom_be.components;


import com.manh.ecom_be.models.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@KafkaListener(id = "groupA", topics = {"get-all-categories", "insert-a-category"})
@Slf4j
public class MyKafkaListener {
    @KafkaHandler
    public void listenCategory(Category category) {
        log.info("Received category event: {}", category);
    }

    @KafkaHandler(isDefault = true)
    public void unknown(Object object) {
        log.warn("Received unknown event type: {}", object);
    }

    @KafkaHandler
    public void listenListOfCategories(List<Category> categories) {
        log.info("Received categories list event: {} categories", categories.size());
    }
}
