package com.rustem.eduthesis.infrastructure.service;

import com.rustem.eduthesis.api.dto.EnrollmentResponse;
import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.EnrollmentEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.CourseNotFoundException;
import com.rustem.eduthesis.infrastructure.exception.EnrollmentAlreadyExistsException;
import com.rustem.eduthesis.infrastructure.exception.EnrollmentNotFoundException;
import com.rustem.eduthesis.infrastructure.exception.UnauthorizedAccessException;
import com.rustem.eduthesis.infrastructure.mapper.EnrollmentMapper;
import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.EnrollmentRepository;
import com.rustem.eduthesis.infrastructure.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepo;
    private final CourseRepository courseRepo;
    private final ProgressRepository progressRepo;
    private final AuthenticationService authService;
    private final EnrollmentMapper mapper;

    @Transactional
    public void enrollCurrentUserInCourse(Long courseId) {
        UserEntity student = authService.getCurrentUser();
        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));

        boolean isStudent = student.getRoles().stream()
                .anyMatch(role -> role.getName().equals("STUDENT"));

        if (!isStudent) {
            throw new UnauthorizedAccessException("Only students can enroll in courses");
        }

        if (!course.isPublished()) {
            throw new UnauthorizedAccessException("Cannot enroll in an unpublished course");
        }

        if (enrollmentRepo.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new EnrollmentAlreadyExistsException("Already enrolled in this course");
        }

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .student(student)
                .course(course)
                .build();
        enrollmentRepo.save(enrollment);
    }

    @Transactional
    public void unenrollCurrentUserFromCourse(Long courseId) {
        UserEntity student = authService.getCurrentUser();

        if (!courseRepo.existsById(courseId)) {
            throw new CourseNotFoundException("Course not found with ID: " + courseId);
        }

        EnrollmentEntity enrollment = enrollmentRepo.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Not enrolled in this course"));

        progressRepo.deleteByStudentIdAndCourseId(student.getId(), courseId);

        enrollmentRepo.delete(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsForCurrentUser() {
        UserEntity student = authService.getCurrentUser();
        List<EnrollmentEntity> enrollments = enrollmentRepo.findByStudentId(student.getId());

        return enrollments.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsForCourse(Long courseId) {
        if (!courseRepo.existsById(courseId)) {
            throw new CourseNotFoundException("Course not found with ID: " + courseId);
        }

        List<EnrollmentEntity> enrollments = enrollmentRepo.findByCourseId(courseId);

        return enrollments.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
