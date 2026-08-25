package com.manh.ecom_be.controllers;

import com.manh.ecom_be.components.LocalizationUtils;
import com.manh.ecom_be.components.converters.CategoryMessageConverter;
import com.manh.ecom_be.dtos.CategoryDTO;
import com.manh.ecom_be.models.Category;
import com.manh.ecom_be.responses.ApiResponse;
import com.manh.ecom_be.services.category.CategoryService;
import com.manh.ecom_be.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final LocalizationUtils localizationUtils;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO
    ) {
        Category category = categoryService.createCategory(categoryDTO);
        this.kafkaTemplate.send("insert-a-category", category);
        this.kafkaTemplate.setMessageConverter(new CategoryMessageConverter());
        return ResponseEntity.ok(ApiResponse.created(category, "Create category successfully"));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ) {
        List<Category> categories = categoryService.getAllCategories();
        kafkaTemplate.send("get-all-categories", categories);
        return ResponseEntity.ok(ApiResponse.success(categories, "Get categories successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(
            @PathVariable("id") Long categoryId)
    {
        Category existingCategory = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success(existingCategory, "Get category information successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO
    ) {
        categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.getCategoryById(id),
                localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteCategory(@PathVariable Long id) throws Exception {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Delete category successfully"));
    }
}
