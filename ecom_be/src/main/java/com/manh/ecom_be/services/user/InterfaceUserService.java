package com.manh.ecom_be.services.user;

import com.manh.ecom_be.dtos.UpdateUserDTO;
import com.manh.ecom_be.dtos.UserDTO;
import com.manh.ecom_be.dtos.UserLoginDTO;
import com.manh.ecom_be.models.User;

public interface InterfaceUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(UserLoginDTO userLoginDTO) throws Exception;
    User getUserDetailsFromToken(String token) throws Exception;
    User updateUser(Long userId, UpdateUserDTO dto) throws Exception;
}
