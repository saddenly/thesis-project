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
public class LessonResponse {
    private Long id;
    private String title;
    private String content;
    private String videoUrl;
    private Integer orderIndex;
    private Integer durationMinutes;
    private String additionalResources;
    private SimpleCourseDTO course;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
