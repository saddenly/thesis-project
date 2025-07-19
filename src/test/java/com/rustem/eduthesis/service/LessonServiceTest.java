package com.rustem.eduthesis.service;

import com.rustem.eduthesis.api.dto.LessonRequest;
import com.rustem.eduthesis.api.dto.LessonResponse;
import com.rustem.eduthesis.api.dto.SimpleCourseDTO;
import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.LessonEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.CourseNotFoundException;
import com.rustem.eduthesis.infrastructure.exception.LessonNotFoundException;
import com.rustem.eduthesis.infrastructure.mapper.LessonMapper;
import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.LessonRepository;
import com.rustem.eduthesis.infrastructure.service.AuthenticationService;
import com.rustem.eduthesis.infrastructure.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AuthenticationService authService;

    @Mock
    private LessonMapper lessonMapper;

    @InjectMocks
    private LessonService lessonService;

    private LessonEntity lessonEntity;
    private LessonResponse lessonResponse;
    private LessonRequest lessonRequest;
    private CourseEntity courseEntity;
    private UserEntity instructor;
    private UserEntity anotherInstructor;

    @BeforeEach
    void setUp() {
        // Set up test data
        instructor = new UserEntity();
        instructor.setId(1L);
        instructor.setEmail("instructor@example.com");
        instructor.setFirstName("Instructor");
        instructor.setLastName("Test");

        anotherInstructor = new UserEntity();
        anotherInstructor.setId(2L);
        anotherInstructor.setEmail("another@example.com");
        anotherInstructor.setFirstName("Another");
        anotherInstructor.setLastName("Instructor");

        courseEntity = new CourseEntity();
        courseEntity.setId(101L);
        courseEntity.setTitle("Java Programming");
        courseEntity.setDescription("Learn Java from scratch");
        courseEntity.setInstructor(instructor);
        courseEntity.setCreatedAt(LocalDateTime.now());
        courseEntity.setUpdatedAt(LocalDateTime.now());

        lessonEntity = new LessonEntity();
        lessonEntity.setId(201L);
        lessonEntity.setTitle("Introduction to Java");
        lessonEntity.setContent("Java is a popular programming language...");
        lessonEntity.setOrderIndex(1);
        lessonEntity.setCourse(courseEntity);
        lessonEntity.setCreatedAt(LocalDateTime.now());
        lessonEntity.setUpdatedAt(LocalDateTime.now());

        lessonResponse = new LessonResponse();
        lessonResponse.setId(201L);
        lessonResponse.setTitle("Introduction to Java");
        lessonResponse.setContent("Java is a popular programming language...");
        lessonResponse.setOrderIndex(1);
        lessonResponse.setCourse(SimpleCourseDTO.builder()
                        .id(101L)
                        .title("Java Programming")
                .build());
        lessonResponse.setCreatedAt(LocalDateTime.now());
        lessonResponse.setUpdatedAt(LocalDateTime.now());

        lessonRequest = new LessonRequest();
        lessonRequest.setTitle("New Lesson");
        lessonRequest.setContent("This is the content of the new lesson");
        lessonRequest.setOrderIndex(2);
    }

    @Test
    void getLessonsForCourse_withValidCourseId_shouldReturnLessons() {
        // Arrange
        List<LessonEntity> lessons = Collections.singletonList(lessonEntity);
        when(courseRepository.findById(101L)).thenReturn(Optional.of(courseEntity));
        when(lessonRepository.findByCourseOrderByOrderIndexAsc(courseEntity)).thenReturn(lessons);
        when(lessonMapper.toResponse(any(LessonEntity.class))).thenReturn(lessonResponse);

        // Act
        List<LessonResponse> result = lessonService.getLessonsForCourse(101L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Introduction to Java");

        verify(courseRepository).findById(101L);
        verify(lessonRepository).findByCourseOrderByOrderIndexAsc(courseEntity);
        verify(lessonMapper).toResponse(any(LessonEntity.class));
    }

    @Test
    void getLessonsForCourse_withNonExistentCourseId_shouldThrowException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CourseNotFoundException.class, () -> lessonService.getLessonsForCourse(999L));

        verify(courseRepository).findById(999L);
        verify(lessonRepository, never()).findByCourseIdOrderByOrderIndex(anyLong());
    }

    @Test
    void getLessonById_withValidIds_shouldReturnLesson() {
        // Arrange
        when(courseRepository.existsById(101L)).thenReturn(true);
        when(lessonRepository.findByIdAndCourseId(201L, 101L)).thenReturn(Optional.of(lessonEntity));
        when(lessonMapper.toResponse(lessonEntity)).thenReturn(lessonResponse);

        // Act
        LessonResponse result = lessonService.getLessonById(101L, 201L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(201L);
        assertThat(result.getTitle()).isEqualTo("Introduction to Java");

        verify(courseRepository).existsById(101L);
        verify(lessonRepository).findByIdAndCourseId(201L, 101L);
        verify(lessonMapper).toResponse(lessonEntity);
    }

    @Test
    void getLessonById_withNonExistentCourseId_shouldThrowException() {
        // Arrange
        when(courseRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(CourseNotFoundException.class, () -> lessonService.getLessonById(999L, 201L));

        verify(courseRepository).existsById(999L);
        verify(lessonRepository, never()).findByIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    void getLessonById_withNonExistentLessonId_shouldThrowException() {
        // Arrange
        when(courseRepository.existsById(101L)).thenReturn(true);
        when(lessonRepository.findByIdAndCourseId(999L, 101L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LessonNotFoundException.class, () -> lessonService.getLessonById(101L, 999L));

        verify(courseRepository).existsById(101L);
        verify(lessonRepository).findByIdAndCourseId(999L, 101L);
    }

    @Test
    void createLesson_shouldCreateAndReturnLesson() {
        // Arrange
        when(courseRepository.findById(101L)).thenReturn(Optional.of(courseEntity));
        when(lessonRepository.save(any(LessonEntity.class))).thenAnswer(i -> {
            LessonEntity saved = i.getArgument(0);
            saved.setId(201L);
            return saved;
        });
        when(lessonMapper.toEntity(any(LessonRequest.class))).thenReturn(lessonEntity);
        when(lessonMapper.toResponse(any(LessonEntity.class))).thenReturn(lessonResponse);

        // Act
        LessonResponse result = lessonService.createLesson(101L, lessonRequest);

        // Assert
        ArgumentCaptor<LessonEntity> lessonCaptor = ArgumentCaptor.forClass(LessonEntity.class);
        verify(lessonRepository).save(lessonCaptor.capture());

        LessonEntity capturedLesson = lessonCaptor.getValue();
        assertThat(capturedLesson.getTitle()).isEqualTo("Introduction to Java");
        assertThat(capturedLesson.getContent()).isEqualTo("Java is a popular programming language...");
        assertThat(capturedLesson.getOrderIndex()).isEqualTo(2);
        assertThat(capturedLesson.getCourse()).isEqualTo(courseEntity);
        assertThat(capturedLesson.getCreatedAt()).isNotNull();

        assertThat(result).isEqualTo(lessonResponse);
    }

    @Test
    void createLesson_withNonExistentCourseId_shouldThrowException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CourseNotFoundException.class, () -> lessonService.createLesson(999L, lessonRequest));

        verify(courseRepository).findById(999L);
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void updateLesson_whenUserIsInstructor_shouldUpdateAndReturnLesson() {
        // Arrange
        when(courseRepository.existsById(101L)).thenReturn(true);
        when(lessonRepository.findByIdAndCourseId(201L, 101L)).thenReturn(Optional.of(lessonEntity));
        when(lessonMapper.toResponse(lessonEntity)).thenReturn(lessonResponse);

        // Act
        LessonResponse result = lessonService.updateLesson(101L, 201L, lessonRequest);

        // Assert
        ArgumentCaptor<LessonEntity> lessonCaptor = ArgumentCaptor.forClass(LessonEntity.class);
        verify(lessonMapper).toResponse(lessonCaptor.capture());

        LessonEntity capturedLesson = lessonCaptor.getValue();
        assertThat(capturedLesson.getTitle()).isEqualTo("New Lesson");
        assertThat(capturedLesson.getContent()).isEqualTo("This is the content of the new lesson");
        assertThat(capturedLesson.getOrderIndex()).isEqualTo(2);
        assertThat(capturedLesson.getUpdatedAt()).isNotNull();

        assertThat(result).isEqualTo(lessonResponse);
    }

    @Test
    void updateLesson_withNonExistentCourseId_shouldThrowException() {
        // Arrange
        when(courseRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(CourseNotFoundException.class, () -> lessonService.updateLesson(999L, 201L, lessonRequest));

        verify(courseRepository).existsById(999L);
        verify(lessonRepository, never()).findByIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    void updateLesson_withNonExistentLessonId_shouldThrowException() {
        // Arrange
        when(courseRepository.existsById(101L)).thenReturn(true);
        when(lessonRepository.findByIdAndCourseId(999L, 101L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LessonNotFoundException.class, () -> lessonService.updateLesson(101L, 999L, lessonRequest));

        verify(courseRepository).existsById(101L);
        verify(lessonRepository).findByIdAndCourseId(999L, 101L);
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void deleteLesson_whenUserIsInstructor_shouldDeleteLesson() {
        // Arrange
        when(courseRepository.existsById(101L)).thenReturn(true);
        when(lessonRepository.findByIdAndCourseId(201L, 101L)).thenReturn(Optional.of(lessonEntity));
        doNothing().when(lessonRepository).delete(lessonEntity);

        // Act
        lessonService.deleteLesson(101L, 201L);

        // Assert
        verify(courseRepository).existsById(101L);
        verify(lessonRepository).findByIdAndCourseId(201L, 101L);
        verify(lessonRepository).delete(lessonEntity);
    }

    @Test
    void deleteLesson_withNonExistentCourseId_shouldThrowException() {
        // Arrange
        when(courseRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(CourseNotFoundException.class, () -> lessonService.deleteLesson(999L, 201L));

        verify(courseRepository).existsById(999L);
        verify(lessonRepository, never()).findByIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    void deleteLesson_withNonExistentLessonId_shouldThrowException() {
        // Arrange
        when(courseRepository.existsById(101L)).thenReturn(true);
        when(lessonRepository.findByIdAndCourseId(999L, 101L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LessonNotFoundException.class, () -> lessonService.deleteLesson(101L, 999L));

        verify(courseRepository).existsById(101L);
        verify(lessonRepository).findByIdAndCourseId(999L, 101L);
        verify(lessonRepository, never()).delete(any());
    }
}
