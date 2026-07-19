package com.manh.ecom_be.repositories;

import com.manh.ecom_be.models.Comment;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {


    List<Comment> findByUserIdAndProductId(
            @Param("userId") Long userId,
            @Param("productId") Long productId
    );

    List<Comment> findByProductId(@Param("productId") Long productId);
}
