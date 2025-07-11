package com.rustem.eduthesis.infrastructure.mapper;

import com.rustem.eduthesis.api.dto.UserResponse;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(UserEntity entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
