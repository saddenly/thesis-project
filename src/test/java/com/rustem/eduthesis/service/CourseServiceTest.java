package com.rustem.eduthesis.service;

import com.rustem.eduthesis.api.dto.CourseRequest;
import com.rustem.eduthesis.api.dto.CourseResponse;
import com.rustem.eduthesis.api.dto.SimpleUserDTO;
import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.CourseNotFoundException;
import com.rustem.eduthesis.infrastructure.mapper.CourseMapper;
import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseService courseService;

    private CourseEntity courseEntity;
    private CourseResponse courseResponse;
    private CourseRequest courseRequest;

    @BeforeEach
    void setUp() {
        // Set up test data
        UserEntity instructor = new UserEntity();
        instructor.setId(1L);
        instructor.setEmail("instructor@example.com");
        instructor.setFirstName("Instructor");
        instructor.setLastName("Test");
        instructor.setRoles(new HashSet<>(Collections.singletonList(RoleEntity.builder().id(2L).name("INSTRUCTOR").build())));

        UserEntity anotherInstructor = new UserEntity();
        anotherInstructor.setId(2L);
        anotherInstructor.setEmail("another@example.com");
        anotherInstructor.setFirstName("Another");
        anotherInstructor.setLastName("Instructor");
        instructor.setRoles(new HashSet<>(Collections.singletonList(RoleEntity.builder().id(2L).name("INSTRUCTOR").build())));

        courseEntity = new CourseEntity();
        courseEntity.setId(101L);
        courseEntity.setTitle("Java Programming");
        courseEntity.setDescription("Learn Java from scratch");
        courseEntity.setInstructor(instructor);
        courseEntity.setPublished(true);
        courseEntity.setCreatedAt(LocalDateTime.now());
        courseEntity.setUpdatedAt(LocalDateTime.now());

        // Use a builder pattern correctly matching the actual CourseResponse structure
        courseResponse = CourseResponse.builder()
                .id(101L)
                .title("Java Programming")
                .description("Learn Java from scratch")
                .instructor(SimpleUserDTO.builder()
                        .id(1L)
                        .firstName("Instructor")
                        .lastName("Test")
                        .email("instructor@example.com")
                        .build())
                .lessons(Collections.emptyList())
                .build();

        courseRequest = new CourseRequest();
        courseRequest.setTitle("New Course");
        courseRequest.setDescription("This is a new course");
    }

    @Test
    void getAllCourses_shouldReturnOnlyPublishedCourses() {
        // Arrange
        List<CourseEntity> courses = Collections.singletonList(courseEntity);

        when(courseRepository.findByPublishedTrue()).thenReturn(courses);
        when(courseMapper.toResponse(courseEntity)).thenReturn(courseResponse);

        // Act
        List<CourseResponse> result = courseService.getAllPublishedCourses();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Programming");

        verify(courseRepository).findByPublishedTrue();
        verify(courseMapper).toResponse(courseEntity);
    }

    @Test
    void getCourseById_withExistingId_shouldReturnCourse() {
        // Arrange
        when(courseRepository.findById(101L)).thenReturn(Optional.of(courseEntity));
        when(courseMapper.toResponse(courseEntity)).thenReturn(courseResponse);

        // Act
        CourseResponse result = courseService.getCourseById(101L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getTitle()).isEqualTo("Java Programming");

        verify(courseRepository).findById(101L);
    }

    @Test
    void getCourseById_withNonExistingId_shouldThrowException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CourseNotFoundException.class, () ->
                courseService.getCourseById(999L));
    }

    @Test
    void updateCourse_shouldUpdateAndReturnCourse() {
        // Arrange
        when(courseRepository.findById(101L)).thenReturn(Optional.of(courseEntity));
        when(courseRepository.save(any(CourseEntity.class))).thenReturn(courseEntity);
        when(courseMapper.toResponse(courseEntity)).thenReturn(courseResponse);

        // Act
        CourseResponse result = courseService.updateCourse(101L, courseRequest);

        // Assert
        ArgumentCaptor<CourseEntity> courseCaptor = ArgumentCaptor.forClass(CourseEntity.class);
        verify(courseRepository).save(courseCaptor.capture());

        CourseEntity capturedCourse = courseCaptor.getValue();
        assertThat(capturedCourse.getTitle()).isEqualTo("New Course");
        assertThat(capturedCourse.getDescription()).isEqualTo("This is a new course");
        assertThat(capturedCourse.getUpdatedAt()).isNotNull();

        assertThat(result).isEqualTo(courseResponse);
    }

    @Test
    void updateCourse_withNonExistingId_shouldThrowException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CourseNotFoundException.class, () ->
                courseService.updateCourse(999L, courseRequest)
        );
    }

    @Test
    void deleteCourse_shouldDeleteCourse() {
        // Arrange
        when(courseRepository.existsById(101L)).thenReturn(true);

        // Act
        courseService.deleteCourse(101L);

        // Assert
        verify(courseRepository).deleteById(101L);
    }

    @Test
    void deleteCourse_withNonExistingId_shouldThrowException() {
        // Act & Assert
        assertThrows(CourseNotFoundException.class, () ->
                courseService.deleteCourse(999L)
        );
    }
}
