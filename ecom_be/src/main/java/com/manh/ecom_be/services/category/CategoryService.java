package com.manh.ecom_be.services.category;

import com.manh.ecom_be.dtos.CategoryDTO;
import com.manh.ecom_be.models.Category;
import com.manh.ecom_be.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements InterfaceCategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public Category createCategory(CategoryDTO dto) {
        return categoryRepository.save(Category.builder().name(dto.getName()).build());
    }

    @Override
    public Category getCategoryById(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found " + id));
    }

    @Override
    public List<Category> getAllCategories() { return categoryRepository.findAll();
    }

    @Override
    public Category updateCategory(long id, CategoryDTO dto) {
        Category cat = getCategoryById(id);
        cat.setName(dto.getName());
        return categoryRepository.save(cat);
    }

    @Override
    public void deleteCategory(long id) { categoryRepository.deleteById(id);}
}
