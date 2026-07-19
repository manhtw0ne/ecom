package com.manh.ecom_be.responses.favorite;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.manh.ecom_be.models.Favorite;
import lombok.*;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FavoriteResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("user_id")
    private Long userId;

    public static FavoriteResponse fromFavorite(Favorite favorite) {
        FavoriteResponse result = FavoriteResponse.builder()
                .id(favorite.getId())
                .userId(favorite.getUser().getId())
                .productId(favorite.getProduct().getId())
                .build();

        return result;
    }
}