package com.rustem.eduthesis.repository;

import com.rustem.eduthesis.infrastructure.entity.*;
import com.rustem.eduthesis.infrastructure.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProgressRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserEntity student1;
    private UserEntity student2;
    private UserEntity instructor;
    private CourseEntity course1;
    private CourseEntity course2;
    private LessonEntity lesson1;
    private LessonEntity lesson2;
    private LessonEntity lesson3;
    private ProgressEntity progress1;
    private ProgressEntity progress2;
    private ProgressEntity progress3;
    private RoleEntity studentRole;
    private RoleEntity instructorRole;

    @BeforeEach
    void setUp() {
        studentRole = RoleEntity.builder()
                .name("STUDENT")
                .build();
        studentRole = roleRepository.save(studentRole);

        instructorRole = RoleEntity.builder()
                .name("INSTRUCTOR")
                .build();
        instructorRole = roleRepository.save(instructorRole);

        instructor = UserEntity.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .password("password")
                .roles(new HashSet<>(Set.of(instructorRole)))
                .build();
        instructor = userRepository.save(instructor);

        student1 = UserEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .email("student1@example.com")
                .password("password")
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();
        student1 = userRepository.save(student1);

        student2 = UserEntity.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("student2@example.com")
                .password("password")
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();
        student2 = userRepository.save(student2);

        course1 = CourseEntity.builder()
                .title("Java Programming")
                .description("Learn Java programming")
                .instructor(instructor)
                .build();
        course1 = courseRepository.save(course1);

        course2 = CourseEntity.builder()
                .title("Spring Boot")
                .description("Learn Spring Boot")
                .instructor(instructor)
                .build();
        course2 = courseRepository.save(course2);

        lesson1 = LessonEntity.builder()
                .title("Introduction to Java")
                .content("Basic Java concepts")
                .course(course1)
                .orderIndex(1)
                .createdAt(LocalDateTime.now())
                .build();
        lesson1 = lessonRepository.save(lesson1);

        lesson2 = LessonEntity.builder()
                .title("Java Variables")
                .content("Understanding variables in Java")
                .course(course1)
                .orderIndex(2)
                .createdAt(LocalDateTime.now())
                .build();
        lesson2 = lessonRepository.save(lesson2);

        lesson3 = LessonEntity.builder()
                .title("Control Structures")
                .content("If statements and loops")
                .course(course2)
                .orderIndex(1)
                .createdAt(LocalDateTime.now())
                .build();
        lesson3 = lessonRepository.save(lesson3);

        progress1 = ProgressEntity.builder()
                .student(student1)
                .lesson(lesson1)
                .course(course1)
                .completed(true)
                .completedAt(LocalDateTime.now().minusHours(1))
                .build();

        progress2 = ProgressEntity.builder()
                .student(student1)
                .lesson(lesson2)
                .course(course1)
                .completed(false)
                .build();

        progress3 = ProgressEntity.builder()
                .student(student2)
                .lesson(lesson1)
                .course(course1)
                .completed(true)
                .completedAt(LocalDateTime.now().minusMinutes(30))
                .build();

        progress1 = progressRepository.save(progress1);
        progress2 = progressRepository.save(progress2);
        progress3 = progressRepository.save(progress3);

        entityManager.flush();
    }

    @Test
    void findByStudentId_shouldReturnProgressForStudent() {
        // Act
        List<ProgressEntity> progressList = progressRepository.findByStudentId(student1.getId());

        // Assert
        assertThat(progressList).hasSize(2);
        assertThat(progressList).extracting(p -> p.getLesson().getTitle())
                .containsExactlyInAnyOrder("Introduction to Java", "Java Variables");
    }

    @Test
    void findByStudentId_withNonExistentStudent_shouldReturnEmptyList() {
        // Act
        List<ProgressEntity> progressList = progressRepository.findByStudentId(999L);

        // Assert
        assertThat(progressList).isEmpty();
    }

    @Test
    void findByLessonId_shouldReturnProgressForLesson() {
        // Act
        List<ProgressEntity> progressList = progressRepository.findByLessonId(lesson1.getId());

        // Assert
        assertThat(progressList).hasSize(2);
        assertThat(progressList).extracting(p -> p.getStudent().getEmail())
                .containsExactlyInAnyOrder("student1@example.com", "student2@example.com");
    }

    @Test
    void findByLessonId_withNonExistentLesson_shouldReturnEmptyList() {
        // Act
        List<ProgressEntity> progressList = progressRepository.findByLessonId(999L);

        // Assert
        assertThat(progressList).isEmpty();
    }

    @Test
    void findByStudentIdAndLessonId_shouldReturnSpecificProgress() {
        // Act
        Optional<ProgressEntity> found = progressRepository.findByStudentIdAndLessonId(student1.getId(), lesson1.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().isCompleted()).isTrue();
        assertThat(found.get().getCompletedAt()).isNotNull();
    }

    @Test
    void findByStudentIdAndLessonId_withNonExistentCombination_shouldReturnEmpty() {
        // Act
        Optional<ProgressEntity> found = progressRepository.findByStudentIdAndLessonId(student2.getId(), lesson2.getId());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void existsByStudentIdAndLessonId_shouldReturnTrueForExistingProgress() {
        // Act
        boolean exists = progressRepository.existsByStudentIdAndLessonId(student1.getId(), lesson1.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByStudentIdAndLessonId_shouldReturnFalseForNonExistentProgress() {
        // Act
        boolean exists = progressRepository.existsByStudentIdAndLessonId(student2.getId(), lesson2.getId());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void findByStudentIdAndCompleted_shouldReturnCompletedProgress() {
        // Act
        List<ProgressEntity> completedProgress = progressRepository.findByStudentIdAndCompleted(student1.getId(), true);

        // Assert
        assertThat(completedProgress).hasSize(1);
        assertThat(completedProgress.get(0).getLesson().getTitle()).isEqualTo("Introduction to Java");
        assertThat(completedProgress.get(0).isCompleted()).isTrue();
    }

    @Test
    void findByStudentIdAndCompleted_shouldReturnIncompleteProgress() {
        // Act
        List<ProgressEntity> incompleteProgress = progressRepository.findByStudentIdAndCompleted(student1.getId(), false);

        // Assert
        assertThat(incompleteProgress).hasSize(1);
        assertThat(incompleteProgress.get(0).getLesson().getTitle()).isEqualTo("Java Variables");
        assertThat(incompleteProgress.get(0).isCompleted()).isFalse();
    }

    @Test
    void findByLesson_CourseId_shouldReturnProgressForCourse() {
        // Act
        List<ProgressEntity> progressList = progressRepository.findByLesson_CourseId(course1.getId());

        // Assert
        assertThat(progressList).hasSize(3);
        assertThat(progressList).allMatch(p -> p.getLesson().getCourse().getId().equals(course1.getId()));
    }

    @Test
    void findByLesson_CourseId_withNonExistentCourse_shouldReturnEmptyList() {
        // Act
        List<ProgressEntity> progressList = progressRepository.findByLesson_CourseId(999L);

        // Assert
        assertThat(progressList).isEmpty();
    }

    @Test
    void findByStudentIdAndLesson_CourseId_shouldReturnStudentProgressForCourse() {
        // Act
        List<ProgressEntity> progressList = progressRepository.findByStudentIdAndLesson_CourseId(student1.getId(), course1.getId());

        // Assert
        assertThat(progressList).hasSize(2);
        assertThat(progressList).allMatch(p -> p.getStudent().getId().equals(student1.getId()));
        assertThat(progressList).allMatch(p -> p.getLesson().getCourse().getId().equals(course1.getId()));
    }

    @Test
    void countByStudentIdAndLesson_CourseId_shouldReturnCorrectCount() {
        // Act
        long count = progressRepository.countByStudentIdAndLesson_CourseId(student1.getId(), course1.getId());

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByStudentIdAndLesson_CourseId_withNonExistentData_shouldReturnZero() {
        // Act
        long count = progressRepository.countByStudentIdAndLesson_CourseId(student2.getId(), course2.getId());

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void countByStudentIdAndLesson_CourseIdAndCompleted_shouldReturnCompletedCount() {
        // Act
        long completedCount = progressRepository.countByStudentIdAndLesson_CourseIdAndCompleted(student1.getId(), course1.getId(), true);
        long incompleteCount = progressRepository.countByStudentIdAndLesson_CourseIdAndCompleted(student1.getId(), course1.getId(), false);

        // Assert
        assertThat(completedCount).isEqualTo(1);
        assertThat(incompleteCount).isEqualTo(1);
    }

    @Test
    void findByCompletedTrue_shouldReturnOnlyCompletedProgress() {
        // Act
        List<ProgressEntity> completedProgress = progressRepository.findByCompletedTrue();

        // Assert
        assertThat(completedProgress).hasSize(2);
        assertThat(completedProgress).allMatch(ProgressEntity::isCompleted);
        assertThat(completedProgress).allMatch(p -> p.getCompletedAt() != null);
    }

    @Test
    void findByCompletedFalse_shouldReturnOnlyIncompleteProgress() {
        // Act
        List<ProgressEntity> incompleteProgress = progressRepository.findByCompletedFalse();

        // Assert
        assertThat(incompleteProgress).hasSize(1);
        assertThat(incompleteProgress.get(0).isCompleted()).isFalse();
        assertThat(incompleteProgress.get(0).getCompletedAt()).isNull();
    }

    @Test
    void findByStudent_Email_shouldReturnProgressForStudentEmail() {
        // Act
        List<ProgressEntity> progressList = progressRepository.findByStudent_Email("student1@example.com");

        // Assert
        assertThat(progressList).hasSize(2);
        assertThat(progressList).allMatch(p -> p.getStudent().getEmail().equals("student1@example.com"));
    }

    @Test
    void findByStudent_Email_withNonExistentEmail_shouldReturnEmptyList() {
        // Act
        List<ProgressEntity> progressList = progressRepository.findByStudent_Email("nonexistent@example.com");

        // Assert
        assertThat(progressList).isEmpty();
    }

    @Test
    void save_shouldPersistNewProgress() {
        // Arrange
        ProgressEntity newProgress = ProgressEntity.builder()
                .student(student2)
                .lesson(lesson2)
                .course(course2)
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        // Act
        ProgressEntity saved = progressRepository.save(newProgress);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStudent().getEmail()).isEqualTo("student2@example.com");
        assertThat(saved.getLesson().getTitle()).isEqualTo("Java Variables");
        assertThat(saved.isCompleted()).isTrue();
    }

    @Test
    void delete_shouldRemoveProgressFromDatabase() {
        // Arrange
        Long progressId = progress1.getId();

        // Act
        progressRepository.delete(progress1);
        entityManager.flush();

        // Assert
        Optional<ProgressEntity> found = progressRepository.findById(progressId);
        assertThat(found).isEmpty();
    }

    @Test
    void findById_shouldReturnProgressWithStudentAndLessonDetails() {
        // Act
        Optional<ProgressEntity> found = progressRepository.findById(progress1.getId());

        // Assert
        assertThat(found).isPresent();
        ProgressEntity foundProgress = found.get();
        assertThat(foundProgress.getStudent()).isNotNull();
        assertThat(foundProgress.getLesson()).isNotNull();
        assertThat(foundProgress.getStudent().getFirstName()).isEqualTo("John");
        assertThat(foundProgress.getLesson().getTitle()).isEqualTo("Introduction to Java");
    }

    @Test
    void deleteByStudentIdAndLessonId_shouldRemoveSpecificProgress() {
        // Act
        progressRepository.deleteByStudentIdAndLessonId(student1.getId(), lesson1.getId());
        entityManager.flush();

        // Assert
        Optional<ProgressEntity> found = progressRepository.findByStudentIdAndLessonId(student1.getId(), lesson1.getId());
        assertThat(found).isEmpty();

        // Verify other progress still exists
        List<ProgressEntity> remainingProgress = progressRepository.findByStudentId(student1.getId());
        assertThat(remainingProgress).hasSize(1);
    }

    @Test
    void findByLesson_Course_InstructorId_shouldReturnProgressForInstructorCourses() {
        // Act
        List<ProgressEntity> progressList = progressRepository.findByLesson_Course_InstructorId(instructor.getId());

        // Assert
        assertThat(progressList).hasSize(3);
        assertThat(progressList).allMatch(p -> p.getLesson().getCourse().getInstructor().getId().equals(instructor.getId()));
    }

    @Test
    void countByLessonIdAndCompleted_shouldReturnCorrectCounts() {
        // Act
        long completedCount = progressRepository.countByLessonIdAndCompleted(lesson1.getId(), true);
        long incompleteCount = progressRepository.countByLessonIdAndCompleted(lesson1.getId(), false);

        // Assert
        assertThat(completedCount).isEqualTo(2);
        assertThat(incompleteCount).isEqualTo(0);
    }
}