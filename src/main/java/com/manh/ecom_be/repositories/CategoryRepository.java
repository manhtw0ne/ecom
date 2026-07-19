package com.manh.ecom_be.repositories;


import com.manh.ecom_be.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
