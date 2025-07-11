package com.rustem.eduthesis.infrastructure.mapper;

import com.rustem.eduthesis.api.dto.CourseResponse;
import com.rustem.eduthesis.api.dto.SimpleLessonDTO;
import com.rustem.eduthesis.api.dto.SimpleUserDTO;
import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.LessonEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseMapper {

    public CourseResponse toResponse(CourseEntity courseEntity) {
        List<SimpleLessonDTO> lessons = courseEntity.getLessons() != null ?
                courseEntity.getLessons().stream()
                        .map(this::toLessonDto)
                        .toList() : List.of();
        return CourseResponse.builder()
                .id(courseEntity.getId())
                .title(courseEntity.getTitle())
                .description(courseEntity.getDescription())
                .createdAt(courseEntity.getCreatedAt())
                .updatedAt(courseEntity.getUpdatedAt())
                .instructor(toUserDto(courseEntity.getInstructor()))
                .lessons(lessons)
                .enrollmentCount(courseEntity.getEnrollments() != null ? courseEntity.getEnrollments().size() : 0)
                .build();
    }

    private SimpleUserDTO toUserDto(UserEntity userEntity) {
        if (userEntity == null) {
            return null; // Handle case where the user is not set
        }

        return SimpleUserDTO.builder()
                .id(userEntity.getId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .email(userEntity.getEmail())
                .build();
    }

    private SimpleLessonDTO toLessonDto(LessonEntity lessonEntity) {
        return SimpleLessonDTO.builder()
                .id(lessonEntity.getId())
                .title(lessonEntity.getTitle())
                .orderIndex(lessonEntity.getOrderIndex())
                .durationMinutes(lessonEntity.getDurationMinutes())
                .build();
    }
}
