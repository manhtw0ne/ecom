package com.manh.ecom_be.services.category;

import com.manh.ecom_be.dtos.CategoryDTO;
import com.manh.ecom_be.models.Category;

import java.util.List;

public interface InterfaceCategoryService {
    Category createCategory(CategoryDTO category);
    Category getCategoryById(long id);
    List<Category> getAllCategories();
    Category updateCategory(long categoryId, CategoryDTO category);
    Category deleteCategory(long id) throws Exception;

}
