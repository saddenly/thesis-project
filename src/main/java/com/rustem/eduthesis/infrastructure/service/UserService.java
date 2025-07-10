package com.rustem.eduthesis.infrastructure.service;

import com.rustem.eduthesis.api.dto.SignupRequest;
import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.EmailAlreadyExistsException;
import com.rustem.eduthesis.infrastructure.exception.RoleNotFoundException;
import com.rustem.eduthesis.infrastructure.exception.UserNotFoundException;
import com.rustem.eduthesis.infrastructure.repository.RoleRepository;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserEntity registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use: " + signupRequest);
        }

        UserEntity user = UserEntity.builder()
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .provider("local")
                .enabled(true)
                .locked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Set<RoleEntity> roles = new HashSet<>();
        RoleEntity studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RoleNotFoundException("Default STUDENT role not found"));
        roles.add(studentRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Transactional
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Transactional
    public UserEntity getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userDetails.getUsername()));
    }
}
