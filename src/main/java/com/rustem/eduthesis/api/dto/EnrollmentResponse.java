package com.rustem.eduthesis.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    private Long id;
    private SimpleUserDTO student;
    private CourseResponse course;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastAccessedAt;
    private Double progressPercentage;
}
