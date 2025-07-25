package com.rustem.eduthesis.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private SimpleUserDTO instructor;
    private List<SimpleLessonDTO> lessons;
    private Integer enrollmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
