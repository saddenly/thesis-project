package com.rustem.eduthesis.api.controller;

import com.rustem.eduthesis.api.dto.EnrollmentResponse;
import com.rustem.eduthesis.api.dto.MessageResponse;
import com.rustem.eduthesis.infrastructure.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<MessageResponse> enrollInCourse(@PathVariable Long courseId) {
        enrollmentService.enrollCurrentUserInCourse(courseId);
        return ResponseEntity.ok(new MessageResponse("Successfully enrolled in course"));
    }

    @DeleteMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<MessageResponse> unenrollFromCourse(@PathVariable Long courseId) {
        enrollmentService.unenrollCurrentUserFromCourse(courseId);
        return ResponseEntity.ok(new MessageResponse("Successfully unenrolled from course"));
    }

    @GetMapping("/courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsForCurrentUser() {
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsForCurrentUser();
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/courses/{courseId}/students")
    @PreAuthorize("isCourseOwnerOrAdmin(#courseId)")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsForCourse(@PathVariable Long courseId) {
        List<EnrollmentResponse> students = enrollmentService.getEnrollmentsForCourse(courseId);
        return ResponseEntity.ok(students);
    }
}
