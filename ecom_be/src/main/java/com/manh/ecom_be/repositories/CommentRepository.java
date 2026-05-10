package com.manh.ecom_be.repositories;

import com.manh.ecom_be.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByUserIdAndProductId(Long userId, Long productId);

    List<Comment> findByProductId(Long productId);
}
