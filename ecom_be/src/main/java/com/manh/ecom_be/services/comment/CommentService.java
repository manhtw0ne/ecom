package com.manh.ecom_be.services.comment;


import com.manh.ecom_be.dtos.CommentDTO;
import com.manh.ecom_be.models.Comment;
import com.manh.ecom_be.models.Product;
import com.manh.ecom_be.models.User;
import com.manh.ecom_be.repositories.CommentRepository;
import com.manh.ecom_be.repositories.ProductRepository;
import com.manh.ecom_be.repositories.UserRepository;
import com.manh.ecom_be.responses.comment.CommentResponse;
import com.manh.ecom_be.services.product.InterfaceProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService implements InterfaceCommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Comment insertComment(CommentDTO dto) {
        User user = userRepository.findById(dto.getUserId()).orElse(null);
        Product product = productRepository.findById(dto.getProductId()).orElse(null);
        if (user == null || product == null) {
            throw new IllegalArgumentException("User or product not found");
        }
        return commentRepository.save(Comment.builder()
                .user(user)
                .product(product)
                .content(dto.getContent())
                .build());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void updateComment(Long id, CommentDTO dto) throws DataNotFoundException {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Comment not found"));
        comment.setContent(dto.getContent());
        commentRepository.save(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByProduct(Long productId) {
        return commentRepository.findByProductId(productId).stream()
                .map(CommentResponse::fromComment).collect(Collectors.toList());
    }

    @Override
    public List<CommentResponse> getCommentsByUserAndProduct(Long userId, Long productId) {
        return commentRepository.findByUserIdAndProductId(userId, productId).stream()
                .map(CommentResponse::fromComment).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void generateFakeComments() throws Exception {
        Faker faker = new Faker();
        Random random = new Random();
        List<User> users = userRepository.findAll();
        List<Product> products = productRepository.findAll();
        List<Comment> batch = new ArrayList<>();
        final int batchSize = 1000;

        for (int i = 0; i < 10_000; i++) {
            User user = users.get(random.nextInt(users.size()));
            Product product = products.get(random.nextInt(products.size()));

            Comment comment = Comment.builder()
                    .content(faker.lorem().sentence())
                    .product(product)
                    .user(user)
                    .build();

            LocalDateTime start = LocalDateTime.of(2015, 1, 1, 0, 0);
            long randomEpoch = ThreadLocalRandom.current()
                    .nextLong(start.toEpochSecond(ZoneOffset.UTC),
                            LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            comment.setCreatedAt(LocalDateTime.ofEpochSecond(randomEpoch, 0, ZoneOffset.UTC));

            batch.add(comment);
            if (batch.size() >= batchSize) {
                commentRepository.saveAll(batch);
                batch.clear();
            }
        }
    }
}
