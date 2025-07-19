package com.rustem.eduthesis.service;

import com.rustem.eduthesis.api.dto.EnrollmentResponse;
import com.rustem.eduthesis.api.dto.SimpleUserDTO;
import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.EnrollmentEntity;
import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.CourseNotFoundException;
import com.rustem.eduthesis.infrastructure.exception.EnrollmentAlreadyExistsException;
import com.rustem.eduthesis.infrastructure.exception.EnrollmentNotFoundException;
import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.EnrollmentRepository;
import com.rustem.eduthesis.infrastructure.repository.ProgressRepository;
import com.rustem.eduthesis.infrastructure.service.AuthenticationService;
import com.rustem.eduthesis.infrastructure.service.EnrollmentService;
import com.rustem.eduthesis.infrastructure.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AuthenticationService authService;

    @Mock
    private ProgressRepository progressRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private UserEntity student;
    private CourseEntity course;
    private EnrollmentEntity enrollment;

    @BeforeEach
    void setUp() {
        // Set up test data
        student = new UserEntity();
        student.setId(1L);
        student.setEmail("student@example.com");
        student.setFirstName("Student");
        student.setLastName("Test");
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(RoleEntity.builder()
                .id(1L)
                .name("STUDENT")
                .build());
        student.setRoles(roles);

        course = new CourseEntity();
        course.setId(101L);
        course.setTitle("Java Programming");
        course.setDescription("Learn Java from scratch");
        course.setPublished(true);

        enrollment = new EnrollmentEntity();
        enrollment.setId(201L);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());

        EnrollmentResponse enrollmentResponse = new EnrollmentResponse();
        enrollmentResponse.setId(201L);
        enrollmentResponse.setStudent(SimpleUserDTO.builder().id(1L).firstName("Student").lastName("Test")
                .build());
    }

    @Test
    void enrollStudentInCourse_withValidCourseAndStudent_shouldCreateEnrollment() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);
        when(courseRepository.findById(101L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.save(any(EnrollmentEntity.class))).thenAnswer(invocation -> {
            EnrollmentEntity enrollment = invocation.getArgument(0);
            enrollment.setEnrolledAt(LocalDateTime.now());
            return enrollment;
        });

        // Act
        enrollmentService.enrollCurrentUserInCourse(101L);

        // Assert
        ArgumentCaptor<EnrollmentEntity> enrollmentCaptor = ArgumentCaptor.forClass(EnrollmentEntity.class);
        verify(enrollmentRepository).save(enrollmentCaptor.capture());

        EnrollmentEntity capturedEnrollment = enrollmentCaptor.getValue();
        assertThat(capturedEnrollment.getStudent()).isEqualTo(student);
        assertThat(capturedEnrollment.getCourse()).isEqualTo(course);
        assertThat(capturedEnrollment.getEnrolledAt()).isNotNull();
    }

    @Test
    void enrollStudentInCourse_whenAlreadyEnrolled_shouldThrowException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);
        when(courseRepository.findById(101L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 101L)).thenReturn(true);

        // Act & Assert
        assertThrows(EnrollmentAlreadyExistsException.class, () ->
                enrollmentService.enrollCurrentUserInCourse(101L)
        );

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudentInCourse_withNonExistentCourse_shouldThrowException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);

        // Act & Assert
        assertThrows(CourseNotFoundException.class, () ->
                enrollmentService.enrollCurrentUserInCourse(999L)
        );
    }

    @Test
    void unenrollStudentFromCourse_withValidEnrollment_shouldDeleteEnrollment() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);
        when(courseRepository.existsById(101L)).thenReturn(true);
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 101L)).thenReturn(Optional.of(enrollment));

        // Act
        enrollmentService.unenrollCurrentUserFromCourse(101L);

        // Assert
        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    void unenrollStudentFromCourse_withNonExistentEnrollment_shouldThrowException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 101L)).thenReturn(Optional.empty());
        when(courseRepository.existsById(101L)).thenReturn(true);

        // Act & Assert
        assertThrows(EnrollmentNotFoundException.class, () ->
                enrollmentService.unenrollCurrentUserFromCourse(101L)
        );

        verify(enrollmentRepository, never()).delete(any());
    }

    @Test
    void getEnrollmentsForCourse_withNonExistentCourse_shouldThrowException() {
        // Act & Assert
        assertThrows(CourseNotFoundException.class, () ->
                enrollmentService.getEnrollmentsForCourse(999L)
        );
    }
}
