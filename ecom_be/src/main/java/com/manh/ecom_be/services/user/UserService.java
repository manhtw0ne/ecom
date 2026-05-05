package com.manh.ecom_be.services.user;


import com.manh.ecom_be.components.JwtTokenUtils;
import com.manh.ecom_be.dtos.UserDTO;
import com.manh.ecom_be.models.Role;
import com.manh.ecom_be.models.User;
import com.manh.ecom_be.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements InterfaceUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtil;

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws Exception {
        if (!userDTO.getPhoneNumber().isBlank()
        && userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
            throw new DataIntegrityViolationException("Phone number already exists");

        }
        if (!userDTO.getEmail().isBlank()
        && userRepository.existByEmail(userDTO.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        if (role.getName().equalsIgnore(Role.ADMIN)) {
            throw new PermissionDenyException("Cannot register with admin role");
        }

        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .email(userDTO.getEmail())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .active(true)
                .role(role)
                .build();

        if (!userDTO.isSocialLogin()) {
            newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            newUser.setPassword(userDTO.getPassword());
        }
        return userRepository.save(newUser);
    }

    @Override
    public String login(UserLoginDTO userLoginDTO) throws Exception {
        Optional<User> optionalUser = Optional.empty();

        if (userLoginDTO.getPhoneNumber() != null && !userLoginDTO.getPhoneNumber().isBlank()) {
            optionalUser = userRepository.findByPhoneNumber
                    (userLoginDTO.getPhoneNumber());
        }

        if (optionalUser.isEmpty() && userLoginDTO.getEmail() != null) {
            optionalUser = userRepository.findByEmail(userLoginDTO.getEmail());
        }

        User user = optionalUser.orElseThrow(() -> new DataNotDoundException("Wrong phone/email or password"));

        if (!user.isActive()) {
            throw new DataNotFoundException ("Account is locked");
        }

        if (user.getGoogleAccountId() == null || user.getGoogleAccountId().isEmpty()) {
            if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Wrong phone number or password");
            }
        }
        return jwtTokenUtil.generateToken(user);
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        String subject = jwtTokenUtil.getSubject(token);
        return userRepository.findByPhoneNumber(subject)
                .or(() -> userRepository.findByEmail(subject))
                .orElseThrow(() -> new DataNotFoundException("User not found"));
    }
}
