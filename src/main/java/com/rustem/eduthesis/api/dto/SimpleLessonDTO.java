package com.rustem.eduthesis.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleLessonDTO {
    private Long id;
    private String title;
    private Integer orderIndex;
    private Integer durationMinutes;
}
