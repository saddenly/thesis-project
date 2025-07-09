package com.rustem.eduthesis.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
    private Long id;
    private String title;
    private String content;
    private String videoUrl;
    private int orderIndex;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
