package com.manh.ecom_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    @Query("SELECT p FROM Product p WHERE " +
    "(:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId) " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")

    Page<Product> searchProducts(
            @Param(categoryId) Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE p.id IN :productIds")
    List<Product> findProductsByIds(@Param("productIds") List<Long> productIds);
}


