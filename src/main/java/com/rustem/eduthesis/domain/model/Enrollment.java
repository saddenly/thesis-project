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
public class Enrollment {
    private Long id;
    private User student;
    private Course course;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private boolean active;

    public boolean isCompleted() {
        return completedAt != null;
    }
}
