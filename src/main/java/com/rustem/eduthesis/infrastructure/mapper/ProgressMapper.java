package com.rustem.eduthesis.infrastructure.mapper;

import com.rustem.eduthesis.api.dto.ProgressResponse;
import com.rustem.eduthesis.api.dto.SimpleCourseDTO;
import com.rustem.eduthesis.api.dto.SimpleLessonDTO;
import com.rustem.eduthesis.api.dto.SimpleUserDTO;
import com.rustem.eduthesis.infrastructure.entity.ProgressEntity;
import org.springframework.stereotype.Component;

@Component
public class ProgressMapper {

    public ProgressResponse toResponse(ProgressEntity entity) {
        return ProgressResponse.builder()
                .id(entity.getId())
                .lesson(entity.getLesson() != null ? SimpleLessonDTO.builder()
                        .id(entity.getLesson().getId())
                        .title(entity.getLesson().getTitle())
                        .orderIndex(entity.getLesson().getOrderIndex())
                        .durationMinutes(entity.getLesson().getDurationMinutes())
                        .build() : null)
                .course(entity.getLesson().getCourse() != null ? SimpleCourseDTO.builder()
                        .id(entity.getLesson().getCourse().getId())
                        .title(entity.getLesson().getCourse().getTitle())
                        .description(entity.getLesson().getCourse().getDescription())
                        .createdAt(entity.getLesson().getCourse().getCreatedAt())
                        .updatedAt(entity.getLesson().getCourse().getUpdatedAt())
                        .instructor(entity.getLesson().getCourse().getInstructor() != null ?
                                SimpleUserDTO.builder()
                                        .id(entity.getLesson().getCourse().getInstructor().getId())
                                        .email(entity.getLesson().getCourse().getInstructor().getEmail())
                                        .firstName(entity.getLesson().getCourse().getInstructor().getFirstName())
                                        .lastName(entity.getLesson().getCourse().getInstructor().getLastName())
                                        .build() : null)
                        .build() : null)
                .completed(entity.isCompleted())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
