package com.manh.ecom_be.components;


import com.manh.ecom_be.models.Category;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@KafkaListener(id = "groupA", topics = {"get-all-categories", "insert-a-category"})
public class MyKafkaListener {
    @KafkaHandler
    public void listenCategory(Category category) {
        System.out.println("[KAFKA] Received category: " + category);
    }

    @KafkaHandler
    public void listenListOfCategories(List<Category> categories) {
        System.out.println("[KAFKA] Received " + categories.size() + " categories");
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object object) {
        System.out.println("[KAFKA] Unknown message type: " + object.getClass().getName());
    }
}
