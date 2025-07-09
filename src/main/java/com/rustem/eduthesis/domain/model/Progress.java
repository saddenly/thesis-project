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
public class Progress {
    private Long id;
    private User user;
    private Lesson lesson;
    private Course course;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
