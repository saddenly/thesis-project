package com.rustem.eduthesis.api.controller;

import com.rustem.eduthesis.api.dto.LessonRequest;
import com.rustem.eduthesis.api.dto.LessonResponse;
import com.rustem.eduthesis.infrastructure.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    public ResponseEntity<List<LessonResponse>> getLessonsForCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(lessonService.getLessonsForCourse(courseId));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonById(
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonService.getLessonById(courseId, lessonId));
    }

    @PostMapping
    @PreAuthorize("isCourseOwnerOrAdmin(#courseId)")
    public ResponseEntity<LessonResponse> createLesson(
            @PathVariable Long courseId,
            @RequestBody @Valid LessonRequest lessonRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lessonService.createLesson(courseId, lessonRequest));
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("isCourseOwnerOrAdmin(#courseId)")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @RequestBody @Valid LessonRequest lessonRequest) {
        return ResponseEntity.ok(lessonService.updateLesson(courseId, lessonId, lessonRequest));
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("isCourseOwnerOrAdmin(#courseId)")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        lessonService.deleteLesson(courseId, lessonId);
        return ResponseEntity.noContent().build();
    }
}
