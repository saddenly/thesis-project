package com.rustem.eduthesis.repository;

import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
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
public class CourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserEntity instructor1;
    private UserEntity instructor2;
    private CourseEntity course1;
    private CourseEntity course2;
    private CourseEntity course3;
    private RoleEntity instructorRole;

    @BeforeEach
    void setUp() {
        instructorRole = RoleEntity.builder()
                .name("INSTRUCTOR")
                .build();
        instructorRole = roleRepository.save(instructorRole);

        instructor1 = UserEntity.builder()
                .email("instructor1@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .roles(Set.of(instructorRole))
                .createdAt(LocalDateTime.now())
                .build();
        instructor1 = userRepository.save(instructor1);

        instructor2 = UserEntity.builder()
                .email("instructor2@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("password123")
                .roles(Set.of(instructorRole))
                .createdAt(LocalDateTime.now())
                .build();
        instructor2 = userRepository.save(instructor2);

        course1 = CourseEntity.builder()
                .title("Java Programming")
                .description("Learn Java programming from basics to advanced")
                .instructor(instructor1)
                .published(true)
                .createdAt(LocalDateTime.now())
                .build();

        course2 = CourseEntity.builder()
                .title("Spring Boot Development")
                .description("Master Spring Boot framework")
                .instructor(instructor1)
                .published(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        course3 = CourseEntity.builder()
                .title("Python Basics")
                .description("Introduction to Python programming")
                .instructor(instructor2)
                .published(false)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        course1 = courseRepository.save(course1);
        course2 = courseRepository.save(course2);
        course3 = courseRepository.save(course3);

        entityManager.flush();
    }

    @Test
    void findByInstructorId_shouldReturnCoursesForInstructor() {
        // Act
        List<CourseEntity> courses = courseRepository.findByInstructorId(instructor1.getId());

        // Assert
        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(CourseEntity::getTitle)
                .containsExactlyInAnyOrder("Java Programming", "Spring Boot Development");
    }

    @Test
    void findByInstructorId_withNonExistentInstructor_shouldReturnEmptyList() {
        // Act
        List<CourseEntity> courses = courseRepository.findByInstructorId(999L);

        // Assert
        assertThat(courses).isEmpty();
    }

    @Test
    void findByPublishedTrue_shouldReturnOnlyPublishedCourses() {
        // Act
        List<CourseEntity> publishedCourses = courseRepository.findByPublishedTrue();

        // Assert
        assertThat(publishedCourses).hasSize(2);
        assertThat(publishedCourses).extracting(CourseEntity::getTitle)
                .containsExactlyInAnyOrder("Java Programming", "Spring Boot Development");
        assertThat(publishedCourses).allMatch(CourseEntity::isPublished);
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldReturnMatchingCourses() {
        // Act
        List<CourseEntity> courses = courseRepository.findByTitleContainingIgnoreCase("java");

        // Assert
        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).getTitle()).isEqualTo("Java Programming");
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldBeCaseInsensitive() {
        // Act
        List<CourseEntity> courses = courseRepository.findByTitleContainingIgnoreCase("SPRING");

        // Assert
        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).getTitle()).isEqualTo("Spring Boot Development");
    }

    @Test
    void findByTitleContainingIgnoreCase_withNoMatches_shouldReturnEmptyList() {
        // Act
        List<CourseEntity> courses = courseRepository.findByTitleContainingIgnoreCase("nonexistent");

        // Assert
        assertThat(courses).isEmpty();
    }

    @Test
    void findByInstructorIdAndPublishedTrue_shouldReturnPublishedCoursesForInstructor() {
        // Act
        List<CourseEntity> courses = courseRepository.findByInstructorIdAndPublishedTrue(instructor1.getId());

        // Assert
        assertThat(courses).hasSize(2);
        assertThat(courses).allMatch(CourseEntity::isPublished);
        assertThat(courses).allMatch(course -> course.getInstructor().getId().equals(instructor1.getId()));
    }

    @Test
    void findByInstructorIdAndPublishedTrue_withUnpublishedInstructor_shouldReturnEmptyList() {
        // Act
        List<CourseEntity> courses = courseRepository.findByInstructorIdAndPublishedTrue(instructor2.getId());

        // Assert
        assertThat(courses).isEmpty();
    }

    @Test
    void findById_shouldReturnCourseWithInstructorDetails() {
        // Act
        Optional<CourseEntity> found = courseRepository.findById(course1.getId());

        // Assert
        assertThat(found).isPresent();
        CourseEntity foundCourse = found.get();
        assertThat(foundCourse.getTitle()).isEqualTo("Java Programming");
        assertThat(foundCourse.getInstructor()).isNotNull();
        assertThat(foundCourse.getInstructor().getFirstName()).isEqualTo("John");
    }

    @Test
    void existsByTitleAndInstructorId_shouldReturnTrueForExistingCourse() {
        // Act
        boolean exists = courseRepository.existsByTitleAndInstructorId("Java Programming", instructor1.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByTitleAndInstructorId_shouldReturnFalseForNonExistentCourse() {
        // Act
        boolean exists = courseRepository.existsByTitleAndInstructorId("Nonexistent Course", instructor1.getId());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void existsByTitleAndInstructorId_shouldReturnFalseForSameTitleDifferentInstructor() {
        // Act
        boolean exists = courseRepository.existsByTitleAndInstructorId("Java Programming", instructor2.getId());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void countByInstructorId_shouldReturnCorrectCount() {
        // Act
        long count = courseRepository.countByInstructorId(instructor1.getId());

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByInstructorId_withNoCoursesInstructor_shouldReturnZero() {
        // Arrange
        UserEntity newInstructor = UserEntity.builder()
                .email("new@example.com")
                .firstName("New")
                .lastName("Instructor")
                .password("password123")
                .roles(Set.of(instructorRole))
                .createdAt(LocalDateTime.now())
                .build();
        newInstructor = userRepository.save(newInstructor);

        // Act
        long count = courseRepository.countByInstructorId(newInstructor.getId());

        // Assert
        assertThat(count).isEqualTo(0);
    }
}