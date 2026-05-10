package com.manh.ecom_be.responses.comment;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.manh.ecom_be.models.Comment;
import com.manh.ecom_be.responses.BaseResponse;
import com.manh.ecom_be.responses.UserResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponse extends BaseResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("content")
    private String content;

    @JsonProperty("user")
    private UserResponse user;

    @JsonProperty("product_id")
    private Long productId;

    public static CommentResponse fromComment(Comment comment) {
        CommentResponse response = CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(UserResponse.fromUser(comment.getUser()))
                .productId(comment.getProduct().getId())
                .build();
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}
