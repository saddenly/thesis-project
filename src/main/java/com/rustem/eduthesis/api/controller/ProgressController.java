package com.rustem.eduthesis.api.controller;

import com.rustem.eduthesis.api.dto.MessageResponse;
import com.rustem.eduthesis.api.dto.ProgressResponse;
import com.rustem.eduthesis.infrastructure.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ProgressResponse>> getMyProgress() {
        List<ProgressResponse> progressForCurrentStudent = progressService.getProgressForCurrentStudent();
        return ResponseEntity.ok(progressForCurrentStudent);
    }

    @GetMapping("/students/{studentId}/courses/{courseId}")
    @PreAuthorize("isCourseOwnerOrAdmin(#courseId)")
    public ResponseEntity<List<ProgressResponse>> getProgressForStudentInCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        List<ProgressResponse> studentProgressInCourse = progressService.getStudentProgressInCourse(courseId, studentId);
        return ResponseEntity.ok(studentProgressInCourse);
    }

    @PatchMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<MessageResponse> markLessonAsCompleted(@PathVariable Long lessonId) {
        progressService.markLessonAsCompleted(lessonId);
        return ResponseEntity.ok(new MessageResponse("Lesson marked as completed"));
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ProgressResponse>> getProgressForCourse(@PathVariable Long courseId) {
        List<ProgressResponse> progressForCourse = progressService.getProgressForCourse(courseId);
        return ResponseEntity.ok(progressForCourse);
    }
}
