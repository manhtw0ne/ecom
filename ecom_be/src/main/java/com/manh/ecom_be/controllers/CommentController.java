package com.manh.ecom_be.controllers;


import com.manh.ecom_be.components.SecurityUtils;
import com.manh.ecom_be.dtos.CommentDTO;
import com.manh.ecom_be.models.User;
import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.responses.comment.CommentResponse;
import com.manh.ecom_be.services.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final SecurityUtils securityUtils;

    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllComments(
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam("product_id") Long productId
    ) {
        List<CommentResponse> responses = userId == null
                ? commentService.getCommentsByProduct(productId)
                : commentService.getCommentsByUserAndProduct(userId, productId);

        return ResponseEntity.ok(ResponseObject.builder()
                .message("Get comments successfully")
                .status(HttpStatus.OK)
                .data(responses)
                .build());
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> insertComment(
            @Valid @RequestBody CommentDTO dto
    ) {
        User loginUser = securityUtils.getLoggedInUser();

        if (!loginUser.getId().equals(dto.getUserId())) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject("You cannot comment as another user",
                            HttpStatus.BAD_REQUEST, null)
                    );
        }
        commentService.insertComment(dto);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Insert comment successfully")
                .status(HttpStatus.OK)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentDTO dto
    ) throws Exception {
        User loginUser = securityUtils.getLoggedInUser();

        if (!loginUser.getId().equals(dto.getUserId())) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject("You cannot update another user's comment",
                            HttpStatus.BAD_REQUEST, null)
                    );
        }

        commentService.updateComment(id, dto);
        return ResponseEntity.ok(new ResponseObject("Update comment successfully",
                        HttpStatus.OK, null)
                );
    }

    @PostMapping("/generateFakeComments")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> generateFakeComments() throws Exception {
        commentService.generateFakeComments();
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Generated fake comments successfully")
                .status(HttpStatus.OK)
                .build()
        );
    }
}
