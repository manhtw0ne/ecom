package com.manh.ecom_be.responses;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.manh.ecom_be.models.Role;
import com.manh.ecom_be.models.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse extends BaseResponse {
    @JsonProperty("fullname")
    private String fullName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String email;
    private String address;

    @JsonProperty("profile_image")
    private String profileImage;

    @JsonProperty("role")
    private Role role;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .address(user.getAddress())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .build();
    }


}
