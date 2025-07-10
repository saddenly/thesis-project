package com.rustem.eduthesis.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank(message = "Course title is required")
    @Size(min = 3, max = 100, message = "Course title should be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Course description is required")
    @Size(min = 10, max = 2000, message = "Course description should be between 10 and 2000 characters")
    private String description;

    private String imageUrl;
}
