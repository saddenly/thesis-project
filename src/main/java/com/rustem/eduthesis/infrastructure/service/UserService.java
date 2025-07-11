package com.rustem.eduthesis.infrastructure.service;

import com.rustem.eduthesis.api.dto.UserRequest;
import com.rustem.eduthesis.api.dto.UserResponse;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.UserNotFoundException;
import com.rustem.eduthesis.infrastructure.mapper.UserMapper;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationService authService;
    private final UserMapper mapper;

    public UserResponse getCurrentUserProfile() {
        UserEntity currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException("Current user not found");
        }

        return mapper.toResponse(currentUser);
    }

    public UserResponse getUserProfileById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        return mapper.toResponse(user);
    }

    public UserResponse updateCurrentUserProfile(UserRequest userRequest) {
        UserEntity currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException("Current user not found");
        }

        // Update user fields
        currentUser.setFirstName(userRequest.getFirstName());
        currentUser.setLastName(userRequest.getLastName());

        // Save the updated user
        UserEntity updatedUser = userRepository.save(currentUser);

        return mapper.toResponse(updatedUser);
    }

    public void deleteUserProfile(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        userRepository.delete(user);
    }
}
