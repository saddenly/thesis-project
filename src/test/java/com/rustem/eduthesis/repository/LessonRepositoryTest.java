package com.rustem.eduthesis.repository;

import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.LessonEntity;
import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.LessonRepository;
import com.rustem.eduthesis.infrastructure.repository.RoleRepository;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LessonRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserEntity instructor;
    private CourseEntity course1;
    private CourseEntity course2;
    private LessonEntity lesson1;
    private LessonEntity lesson2;
    private LessonEntity lesson3;
    private LessonEntity lesson4;
    private RoleEntity instructorRole;

    @BeforeEach
    void setUp() {
        instructorRole = RoleEntity.builder()
                .name("INSTRUCTOR")
                .build();
        instructorRole = roleRepository.save(instructorRole);

        instructor = UserEntity.builder()
                .email("instructor@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("password123")
                .roles(Set.of(instructorRole))
                .createdAt(LocalDateTime.now())
                .build();
        instructor = userRepository.save(instructor);

        course1 = CourseEntity.builder()
                .title("Java Programming")
                .description("Learn Java programming")
                .instructor(instructor)
                .published(true)
                .createdAt(LocalDateTime.now())
                .build();
        course1 = courseRepository.save(course1);

        course2 = CourseEntity.builder()
                .title("Spring Boot Development")
                .description("Master Spring Boot")
                .instructor(instructor)
                .published(true)
                .createdAt(LocalDateTime.now())
                .build();
        course2 = courseRepository.save(course2);

        lesson1 = LessonEntity.builder()
                .title("Introduction to Java")
                .content("This lesson covers Java basics")
                .videoUrl("https://example.com/video1")
                .orderIndex(1)
                .course(course1)
                .createdAt(LocalDateTime.now())
                .build();

        lesson2 = LessonEntity.builder()
                .title("Java Variables")
                .content("Learn about Java variables and data types")
                .videoUrl("https://example.com/video2")
                .orderIndex(2)
                .course(course1)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        lesson3 = LessonEntity.builder()
                .title("Control Structures")
                .content("Understanding if-else and loops")
                .videoUrl("https://example.com/video3")
                .orderIndex(3)
                .course(course1)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        lesson4 = LessonEntity.builder()
                .title("Spring Boot Basics")
                .content("Introduction to Spring Boot framework")
                .videoUrl("https://example.com/video4")
                .orderIndex(1)
                .course(course2)
                .createdAt(LocalDateTime.now().minusHours(3))
                .build();

        lesson1 = lessonRepository.save(lesson1);
        lesson2 = lessonRepository.save(lesson2);
        lesson3 = lessonRepository.save(lesson3);
        lesson4 = lessonRepository.save(lesson4);

        entityManager.flush();
    }

    @Test
    void findByCourseId_shouldReturnLessonsForCourse() {
        // Act
        List<LessonEntity> lessons = lessonRepository.findByCourseId(course1.getId());

        // Assert
        assertThat(lessons).hasSize(3);
        assertThat(lessons).extracting(LessonEntity::getTitle)
                .containsExactlyInAnyOrder("Introduction to Java", "Java Variables", "Control Structures");
    }

    @Test
    void findByCourseId_withNonExistentCourse_shouldReturnEmptyList() {
        // Act
        List<LessonEntity> lessons = lessonRepository.findByCourseId(999L);

        // Assert
        assertThat(lessons).isEmpty();
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldReturnMatchingLessons() {
        // Act
        List<LessonEntity> lessons = lessonRepository.findByTitleContainingIgnoreCase("java");

        // Assert
        assertThat(lessons).hasSize(2);
        assertThat(lessons).extracting(LessonEntity::getTitle)
                .containsExactlyInAnyOrder("Introduction to Java", "Java Variables");
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldBeCaseInsensitive() {
        // Act
        List<LessonEntity> lessons = lessonRepository.findByTitleContainingIgnoreCase("SPRING");

        // Assert
        assertThat(lessons).hasSize(1);
        assertThat(lessons.get(0).getTitle()).isEqualTo("Spring Boot Basics");
    }

    @Test
    void findByTitleContainingIgnoreCase_withNoMatches_shouldReturnEmptyList() {
        // Act
        List<LessonEntity> lessons = lessonRepository.findByTitleContainingIgnoreCase("nonexistent");

        // Assert
        assertThat(lessons).isEmpty();
    }

    @Test
    void findByCourseIdAndOrderIndex_shouldReturnSpecificLesson() {
        // Act
        Optional<LessonEntity> found = lessonRepository.findByCourseIdAndOrderIndex(course1.getId(), 2);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Java Variables");
        assertThat(found.get().getOrderIndex()).isEqualTo(2);
    }

    @Test
    void findByCourseIdAndOrderIndex_withNonExistentIndex_shouldReturnEmpty() {
        // Act
        Optional<LessonEntity> found = lessonRepository.findByCourseIdAndOrderIndex(course1.getId(), 10);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void existsByCourseIdAndOrderIndex_shouldReturnTrueForExistingLesson() {
        // Act
        boolean exists = lessonRepository.existsByCourseIdAndOrderIndex(course1.getId(), 1);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCourseIdAndOrderIndex_shouldReturnFalseForNonExistentLesson() {
        // Act
        boolean exists = lessonRepository.existsByCourseIdAndOrderIndex(course1.getId(), 10);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void countByCourseId_shouldReturnCorrectCount() {
        // Act
        long count1 = lessonRepository.countByCourseId(course1.getId());
        long count2 = lessonRepository.countByCourseId(course2.getId());

        // Assert
        assertThat(count1).isEqualTo(3);
        assertThat(count2).isEqualTo(1);
    }

    @Test
    void countByCourseId_withNonExistentCourse_shouldReturnZero() {
        // Act
        long count = lessonRepository.countByCourseId(999L);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void findByCourse_InstructorId_shouldReturnLessonsForInstructor() {
        // Act
        List<LessonEntity> lessons = lessonRepository.findByCourse_InstructorId(instructor.getId());

        // Assert
        assertThat(lessons).hasSize(4);
        assertThat(lessons).allMatch(lesson -> lesson.getCourse().getInstructor().getId().equals(instructor.getId()));
    }

    @Test
    void findByCourse_InstructorId_withNonExistentInstructor_shouldReturnEmptyList() {
        // Act
        List<LessonEntity> lessons = lessonRepository.findByCourse_InstructorId(999L);

        // Assert
        assertThat(lessons).isEmpty();
    }

    @Test
    void findByVideoUrlIsNotNull_shouldReturnLessonsWithVideos() {
        // Act
        List<LessonEntity> lessons = lessonRepository.findByVideoUrlIsNotNull();

        // Assert
        assertThat(lessons).hasSize(4);
        assertThat(lessons).allMatch(lesson -> lesson.getVideoUrl() != null);
    }

    @Test
    void findByVideoUrlIsNull_shouldReturnLessonsWithoutVideos() {
        // Arrange
        LessonEntity lessonWithoutVideo = LessonEntity.builder()
                .title("Text Only Lesson")
                .content("This lesson has no video")
                .orderIndex(4)
                .course(course1)
                .createdAt(LocalDateTime.now())
                .build();
        lessonRepository.save(lessonWithoutVideo);

        // Act
        List<LessonEntity> lessons = lessonRepository.findByVideoUrlIsNull();

        // Assert
        assertThat(lessons).hasSize(1);
        assertThat(lessons.get(0).getTitle()).isEqualTo("Text Only Lesson");
        assertThat(lessons.get(0).getVideoUrl()).isNull();
    }

    @Test
    void findById_shouldReturnLessonWithCourseDetails() {
        // Act
        Optional<LessonEntity> found = lessonRepository.findById(lesson1.getId());

        // Assert
        assertThat(found).isPresent();
        LessonEntity foundLesson = found.get();
        assertThat(foundLesson.getTitle()).isEqualTo("Introduction to Java");
        assertThat(foundLesson.getCourse()).isNotNull();
        assertThat(foundLesson.getCourse().getTitle()).isEqualTo("Java Programming");
    }

    @Test
    void existsByTitleAndCourseId_shouldReturnTrueForExistingLesson() {
        // Act
        boolean exists = lessonRepository.existsByTitleAndCourseId("Introduction to Java", course1.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByTitleAndCourseId_shouldReturnFalseForNonExistentLesson() {
        // Act
        boolean exists = lessonRepository.existsByTitleAndCourseId("Nonexistent Lesson", course1.getId());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void existsByTitleAndCourseId_shouldReturnFalseForSameTitleDifferentCourse() {
        // Act
        boolean exists = lessonRepository.existsByTitleAndCourseId("Introduction to Java", course2.getId());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_shouldPersistNewLesson() {
        // Arrange
        LessonEntity newLesson = LessonEntity.builder()
                .title("New Java Lesson")
                .content("This is a new lesson")
                .videoUrl("https://example.com/new-video")
                .orderIndex(4)
                .course(course1)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        LessonEntity saved = lessonRepository.save(newLesson);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("New Java Lesson");
        assertThat(saved.getCourse().getId()).isEqualTo(course1.getId());
    }

    @Test
    void delete_shouldRemoveLessonFromDatabase() {
        // Arrange
        Long lessonId = lesson1.getId();

        // Act
        lessonRepository.delete(lesson1);
        entityManager.flush();

        // Assert
        Optional<LessonEntity> found = lessonRepository.findById(lessonId);
        assertThat(found).isEmpty();
    }

    @Test
    void findByContentContainingIgnoreCase_shouldReturnMatchingLessons() {
        // Act
        List<LessonEntity> lessons = lessonRepository.findByContentContainingIgnoreCase("variables");

        // Assert
        assertThat(lessons).hasSize(1);
        assertThat(lessons.get(0).getTitle()).isEqualTo("Java Variables");
    }

    @Test
    void findByOrderIndexGreaterThan_shouldReturnLessonsAfterIndex() {
        // Act
        List<LessonEntity> lessons = lessonRepository.findByOrderIndexGreaterThan(1);

        // Assert
        assertThat(lessons).hasSize(2);
        assertThat(lessons).extracting(LessonEntity::getTitle)
                .containsExactlyInAnyOrder("Java Variables", "Control Structures");
        assertThat(lessons).allMatch(lesson -> lesson.getOrderIndex() > 1);
    }
}