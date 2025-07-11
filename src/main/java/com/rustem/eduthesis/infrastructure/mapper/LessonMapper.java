package com.rustem.eduthesis.infrastructure.mapper;

import com.rustem.eduthesis.api.dto.LessonRequest;
import com.rustem.eduthesis.api.dto.LessonResponse;
import com.rustem.eduthesis.api.dto.SimpleCourseDTO;
import com.rustem.eduthesis.api.dto.SimpleUserDTO;
import com.rustem.eduthesis.infrastructure.entity.LessonEntity;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {

    public LessonResponse toResponse(LessonEntity entity) {
        return LessonResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .orderIndex(entity.getOrderIndex())
                .course(SimpleCourseDTO.builder()
                        .id(entity.getCourse().getId())
                        .title(entity.getCourse().getTitle())
                        .description(entity.getCourse().getDescription())
                        .createdAt(entity.getCourse().getCreatedAt())
                        .updatedAt(entity.getCourse().getUpdatedAt())
                        .instructor(entity.getCourse().getInstructor() != null ? SimpleUserDTO.builder()
                                .id(entity.getCourse().getInstructor().getId())
                                .firstName(entity.getCourse().getInstructor().getFirstName())
                                .lastName(entity.getCourse().getInstructor().getLastName())
                                .email(entity.getCourse().getInstructor().getEmail())
                                .build()
                                : null)
                        .build())
                .build();
    }

    public LessonEntity toEntity(LessonRequest request) {
        return LessonEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .durationMinutes(request.getDurationMinutes())
                .orderIndex(request.getOrderIndex())
                .additionalResources(request.getAdditionalResources())
                .build();
    }
}
