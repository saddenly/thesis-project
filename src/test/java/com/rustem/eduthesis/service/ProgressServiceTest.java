package com.rustem.eduthesis.service;

import com.rustem.eduthesis.api.dto.ProgressResponse;
import com.rustem.eduthesis.api.dto.SimpleCourseDTO;
import com.rustem.eduthesis.api.dto.SimpleLessonDTO;
import com.rustem.eduthesis.infrastructure.entity.*;
import com.rustem.eduthesis.infrastructure.exception.*;
import com.rustem.eduthesis.infrastructure.mapper.ProgressMapper;
import com.rustem.eduthesis.infrastructure.repository.*;
import com.rustem.eduthesis.infrastructure.service.AuthenticationService;
import com.rustem.eduthesis.infrastructure.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgressServiceTest {
    @Mock
    private EnrollmentRepository enrollmentRepo;

    @Mock
    private ProgressRepository progressRepo;

    @Mock
    private LessonRepository lessonRepo;

    @Mock
    private CourseRepository courseRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ProgressMapper mapper;

    @Mock
    private AuthenticationService authService;

    @InjectMocks
    private ProgressService progressService;

    private UserEntity student;
    private UserEntity instructor;
    private CourseEntity course;
    private LessonEntity lesson;
    private EnrollmentEntity enrollment;
    private ProgressEntity progress;
    private ProgressResponse progressResponse;
    private RoleEntity studentRole;

    @BeforeEach
    void setUp() {
        studentRole = RoleEntity.builder()
                .id(1L)
                .name("STUDENT")
                .build();

        RoleEntity instructorRole = RoleEntity.builder()
                .id(2L)
                .name("INSTRUCTOR")
                .build();

        student = UserEntity.builder()
                .id(1L)
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(studentRole))
                .build();

        instructor = UserEntity.builder()
                .id(2L)
                .email("instructor@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .roles(Set.of(instructorRole))
                .build();

        course = CourseEntity.builder()
                .id(101L)
                .title("Java Programming")
                .description("Learn Java programming")
                .instructor(instructor)
                .build();

        lesson = LessonEntity.builder()
                .id(201L)
                .title("Introduction to Java")
                .content("Java is a popular programming language...")
                .orderIndex(1)
                .course(course)
                .createdAt(LocalDateTime.now())
                .build();

        enrollment = EnrollmentEntity.builder()
                .id(1L)
                .student(student)
                .course(course)
                .enrolledAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .build();

        progress = ProgressEntity.builder()
                .id(1L)
                .student(student)
                .lesson(lesson)
                .course(course)
                .completed(false)
                .build();

        progressResponse = ProgressResponse.builder()
                .id(1L)
                .lesson(SimpleLessonDTO.builder()
                        .id(201L)
                        .title("Introduction to Java")
                        .build())
                .course(SimpleCourseDTO.builder()
                        .id(101L)
                        .title("Java Programming")
                        .build())
                .completed(false)
                .build();
    }

    @Test
    void markLessonAsCompleted_shouldMarkLessonAsCompleted() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);
        when(lessonRepo.findById(201L)).thenReturn(Optional.of(lesson));
        when(enrollmentRepo.findByStudentIdAndCourseId(1L, 101L)).thenReturn(Optional.of(enrollment));
        when(progressRepo.findByStudentIdAndLessonId(1L, 201L)).thenReturn(Optional.of(progress));

        // Act
        progressService.markLessonAsCompleted(201L);

        // Assert
        verify(progressRepo).save(progress);
        assertThat(progress.isCompleted()).isTrue();
        assertThat(progress.getCompletedAt()).isNotNull();
        verify(enrollmentRepo).findByStudentIdAndCourseId(1L, 101L);
    }

    @Test
    void markLessonAsCompleted_withNonExistentLesson_shouldThrowException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);
        when(lessonRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> progressService.markLessonAsCompleted(999L))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessage("Lesson not found with ID: 999");
    }

    @Test
    void markLessonAsCompleted_withNoEnrollment_shouldThrowException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);
        when(lessonRepo.findById(201L)).thenReturn(Optional.of(lesson));
        when(enrollmentRepo.findByStudentIdAndCourseId(1L, 101L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> progressService.markLessonAsCompleted(201L))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("Enrollment not found");
    }

    @Test
    void markLessonAsCompleted_withNewProgress_shouldCreateProgress() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);
        when(lessonRepo.findById(201L)).thenReturn(Optional.of(lesson));
        when(enrollmentRepo.findByStudentIdAndCourseId(1L, 101L)).thenReturn(Optional.of(enrollment));
        when(progressRepo.findByStudentIdAndLessonId(1L, 201L)).thenReturn(Optional.empty());

        // Act
        progressService.markLessonAsCompleted(201L);

        // Assert
        verify(progressRepo).save(any(ProgressEntity.class));
    }

    @Test
    void getProgressForCurrentStudent_shouldReturnProgress() {
        // Arrange
        List<ProgressEntity> progressList = List.of(progress);
        when(authService.getCurrentUser()).thenReturn(student);
        when(progressRepo.findByStudentId(1L)).thenReturn(progressList);
        when(mapper.toResponse(progress)).thenReturn(progressResponse);

        // Act
        List<ProgressResponse> result = progressService.getProgressForCurrentStudent();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(progressResponse);
        verify(progressRepo).findByStudentId(1L);
        verify(mapper).toResponse(progress);
    }

    @Test
    void getProgressForCurrentStudent_withNonStudent_shouldThrowException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(instructor);

        // Act & Assert
        assertThatThrownBy(() -> progressService.getProgressForCurrentStudent())
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Only students can track own progress");
    }

    @Test
    void getProgressForCourse_shouldReturnProgressForCourse() {
        // Arrange
        List<ProgressEntity> progressList = List.of(progress);
        when(authService.getCurrentUser()).thenReturn(student);
        when(courseRepo.existsById(101L)).thenReturn(true);
        when(enrollmentRepo.existsByStudentIdAndCourseId(1L, 101L)).thenReturn(true);
        when(progressRepo.findByStudentIdAndCourseId(1L, 101L)).thenReturn(progressList);
        when(mapper.toResponse(progress)).thenReturn(progressResponse);

        // Act
        List<ProgressResponse> result = progressService.getProgressForCourse(101L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(progressResponse);
        verify(progressRepo).findByStudentIdAndCourseId(1L, 101L);
    }

    @Test
    void getProgressForCourse_withNonExistentCourse_shouldThrowException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);
        when(courseRepo.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> progressService.getProgressForCourse(999L))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessage("Course not found with ID: 999");
    }

    @Test
    void getProgressForCourse_withNoEnrollment_shouldThrowException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);
        when(courseRepo.existsById(101L)).thenReturn(true);
        when(enrollmentRepo.existsByStudentIdAndCourseId(1L, 101L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> progressService.getProgressForCourse(101L))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("You must be enrolled in the course to track progress");
    }

    @Test
    void getStudentProgressInCourse_shouldReturnStudentProgress() {
        // Arrange
        List<ProgressEntity> progressList = List.of(progress);
        when(courseRepo.existsById(101L)).thenReturn(true);
        when(userRepo.existsById(1L)).thenReturn(true);
        when(enrollmentRepo.existsByStudentIdAndCourseId(1L, 101L)).thenReturn(true);
        when(progressRepo.findByStudentIdAndCourseId(1L, 101L)).thenReturn(progressList);
        when(mapper.toResponse(progress)).thenReturn(progressResponse);

        // Act
        List<ProgressResponse> result = progressService.getStudentProgressInCourse(101L, 1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(progressResponse);
        verify(progressRepo).findByStudentIdAndCourseId(1L, 101L);
    }

    @Test
    void getStudentProgressInCourse_withNonExistentCourse_shouldThrowException() {
        // Arrange
        when(courseRepo.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> progressService.getStudentProgressInCourse(999L, 1L))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessage("Course not found with ID: 999");
    }

    @Test
    void getStudentProgressInCourse_withNonExistentStudent_shouldThrowException() {
        // Arrange
        when(courseRepo.existsById(101L)).thenReturn(true);
        when(userRepo.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> progressService.getStudentProgressInCourse(101L, 999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Student not found with ID: 999");
    }

    @Test
    void getStudentProgressInCourse_withNoEnrollment_shouldThrowException() {
        // Arrange
        when(courseRepo.existsById(101L)).thenReturn(true);
        when(userRepo.existsById(1L)).thenReturn(true);
        when(enrollmentRepo.existsByStudentIdAndCourseId(1L, 101L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> progressService.getStudentProgressInCourse(101L, 1L))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("Student is not enrolled in the course");
    }
}