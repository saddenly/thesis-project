package com.rustem.eduthesis.infrastructure.mapper;

import com.rustem.eduthesis.api.dto.CourseResponse;
import com.rustem.eduthesis.api.dto.EnrollmentResponse;
import com.rustem.eduthesis.api.dto.SimpleLessonDTO;
import com.rustem.eduthesis.api.dto.SimpleUserDTO;
import com.rustem.eduthesis.infrastructure.entity.EnrollmentEntity;
import com.rustem.eduthesis.infrastructure.repository.ProgressRepository;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    private final ProgressRepository progressRepository;

    public EnrollmentMapper(ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    public EnrollmentResponse toResponse(EnrollmentEntity entity) {
        return EnrollmentResponse.builder()
                .id(entity.getId())
                .student(entity.getStudent() != null ? SimpleUserDTO.builder()
                        .id(entity.getStudent().getId())
                        .email(entity.getStudent().getEmail())
                        .firstName(entity.getStudent().getFirstName())
                        .lastName(entity.getStudent().getLastName())
                        .build() : null)
                .course(entity.getCourse() != null ? CourseResponse.builder()
                        .id(entity.getCourse().getId())
                        .title(entity.getCourse().getTitle())
                        .description(entity.getCourse().getDescription())
                        .instructor(SimpleUserDTO.builder()
                                .id(entity.getCourse().getInstructor().getId())
                                .email(entity.getCourse().getInstructor().getEmail())
                                .firstName(entity.getCourse().getInstructor().getFirstName())
                                .lastName(entity.getCourse().getInstructor().getLastName())
                                .build())
                        .lessons(entity.getCourse().getLessons() != null ? entity.getCourse().getLessons().stream()
                                .map(lesson -> SimpleLessonDTO.builder()
                                        .id(lesson.getId())
                                        .title(lesson.getTitle())
                                        .orderIndex(lesson.getOrderIndex())
                                        .durationMinutes(lesson.getDurationMinutes())
                                        .build())
                                .toList() : null)
                        .build() : null)
                .enrolledAt(entity.getEnrolledAt())
                .lastAccessedAt(entity.getLastAccessedAt())
                .progressPercentage(getProgressPercentage(entity))
                .build();
    }

    private Double getProgressPercentage(EnrollmentEntity enrollment) {
        long totalLessons = enrollment.getCourse().getLessons() != null
                ? enrollment.getCourse().getLessons().size()
                : 0L;
        Long completedLessons = progressRepository.countByStudentIdAndCourseIdAndCompletedTrue(
                enrollment.getStudent().getId(), enrollment.getCourse().getId()
        );

        double progressPercentage = totalLessons > 0
                ? (completedLessons.doubleValue() / totalLessons) * 100
                : 0.0;
        return Math.round(progressPercentage * 100.0) / 100.0;
    }
}
