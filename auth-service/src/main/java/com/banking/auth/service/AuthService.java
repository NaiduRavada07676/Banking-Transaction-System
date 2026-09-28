package com.banking.auth.service;

import com.banking.auth.entity.User;
import com.banking.auth.repository.UserRepository;
import com.banking.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(String username, String password, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessException("Username already exists", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role == null ? "USER" : role)
                .build();
        userRepository.save(user);

        return "User registered successfully";
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Invalid username or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        return jwtService.generateToken(user.getUsername(), user.getRole());
    }
}
