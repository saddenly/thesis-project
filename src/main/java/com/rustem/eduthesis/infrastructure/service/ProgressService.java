package com.rustem.eduthesis.infrastructure.service;

import com.rustem.eduthesis.api.dto.ProgressResponse;
import com.rustem.eduthesis.infrastructure.entity.*;
import com.rustem.eduthesis.infrastructure.exception.*;
import com.rustem.eduthesis.infrastructure.mapper.ProgressMapper;
import com.rustem.eduthesis.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private final EnrollmentRepository enrollmentRepo;
    private final ProgressRepository progressRepo;
    private final LessonRepository lessonRepo;
    private final CourseRepository courseRepo;
    private final UserRepository userRepo;
    private final ProgressMapper mapper;
    private final AuthenticationService authService;

    @Transactional
    public void markLessonAsCompleted(Long lessonId) {
        UserEntity student = authService.getCurrentUser();

        LessonEntity lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found with ID: " + lessonId));

        CourseEntity course = lesson.getCourse();

        EnrollmentEntity enrollment = enrollmentRepo.findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found"));
        enrollment.setLastAccessedAt(LocalDateTime.now());

        ProgressEntity progress = progressRepo
                .findByStudentIdAndLessonId(student.getId(), lessonId)
                .orElse(ProgressEntity.builder()
                        .student(student)
                        .lesson(lesson)
                        .course(course)
                        .build());
        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            progressRepo.save(progress);
        }
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> getProgressForCurrentStudent() {
        UserEntity student = authService.getCurrentUser();

        boolean isStudent = student.getRoles().stream()
                .anyMatch(role -> role.getName().equals("STUDENT"));

        if (!isStudent) throw new RoleNotFoundException("Only students can track own progress");

        List<ProgressEntity> progressList = progressRepo.findByStudentId(student.getId());

        return progressList.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> getProgressForCourse(Long courseId) {
        UserEntity student = authService.getCurrentUser();

        if (!courseRepo.existsById(courseId)) {
            throw new CourseNotFoundException("Course not found with ID: " + courseId);
        }

        if (!enrollmentRepo.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new EnrollmentNotFoundException("You must be enrolled in the course to track progress");
        }

        List<ProgressEntity> progressList = progressRepo.findByStudentIdAndCourseId(student.getId(), courseId);

        return progressList.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> getStudentProgressInCourse(Long courseId, Long studentId) {
        if (!courseRepo.existsById(courseId)) throw new CourseNotFoundException("Course not found with ID: " + courseId);

        if (!userRepo.existsById(studentId)) throw new UserNotFoundException("Student not found with ID: " + studentId);

        if (!enrollmentRepo.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new EnrollmentNotFoundException("Student is not enrolled in the course");
        }

        List<ProgressEntity> progressList = progressRepo.findByStudentIdAndCourseId(studentId, courseId);

        return progressList.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
