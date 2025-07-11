package com.rustem.eduthesis.infrastructure.service;

import com.rustem.eduthesis.api.dto.SignupRequest;
import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.EmailAlreadyExistsException;
import com.rustem.eduthesis.infrastructure.exception.RoleNotFoundException;
import com.rustem.eduthesis.infrastructure.exception.UserNotFoundException;
import com.rustem.eduthesis.infrastructure.repository.RoleRepository;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserEntity registerUser(SignupRequest signupRequest) {
        if (userRepo.existsByEmail(signupRequest.getEmail())) {
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
        RoleEntity studentRole = roleRepo.findByName("STUDENT")
                .orElseThrow(() -> new RoleNotFoundException("Default STUDENT role not found"));
        roles.add(studentRole);
        user.setRoles(roles);

        return userRepo.save(user);
    }

    public UserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }

    public UserEntity getCurrentUser() {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            return null;
        }

        return userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
    }
}
