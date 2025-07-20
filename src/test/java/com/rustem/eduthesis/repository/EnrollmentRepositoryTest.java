package com.rustem.eduthesis.repository;

import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.EnrollmentEntity;
import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.EnrollmentRepository;
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
public class EnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserEntity student1;
    private UserEntity student2;
    private UserEntity instructor;
    private CourseEntity course1;
    private CourseEntity course2;
    private EnrollmentEntity enrollment1;
    private EnrollmentEntity enrollment2;
    private EnrollmentEntity enrollment3;
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
                .email("instructor@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("password123")
                .roles(Set.of(instructorRole))
                .createdAt(LocalDateTime.now())
                .build();
        instructor = userRepository.save(instructor);

        student1 = UserEntity.builder()
                .email("student1@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .roles(Set.of(studentRole))
                .createdAt(LocalDateTime.now())
                .build();
        student1 = userRepository.save(student1);

        student2 = UserEntity.builder()
                .email("student2@example.com")
                .firstName("Alice")
                .lastName("Johnson")
                .password("password123")
                .roles(Set.of(studentRole))
                .createdAt(LocalDateTime.now())
                .build();
        student2 = userRepository.save(student2);

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

        enrollment1 = EnrollmentEntity.builder()
                .student(student1)
                .course(course1)
                .enrolledAt(LocalDateTime.now())
                .build();

        enrollment2 = EnrollmentEntity.builder()
                .student(student1)
                .course(course2)
                .enrolledAt(LocalDateTime.now().minusDays(1))
                .build();

        enrollment3 = EnrollmentEntity.builder()
                .student(student2)
                .course(course1)
                .enrolledAt(LocalDateTime.now().minusDays(2))
                .build();

        enrollment1 = enrollmentRepository.save(enrollment1);
        enrollment2 = enrollmentRepository.save(enrollment2);
        enrollment3 = enrollmentRepository.save(enrollment3);

        entityManager.flush();
    }

    @Test
    void findByStudentId_shouldReturnEnrollmentsForStudent() {
        // Act
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByStudentId(student1.getId());

        // Assert
        assertThat(enrollments).hasSize(2);
        assertThat(enrollments).extracting(e -> e.getCourse().getTitle())
                .containsExactlyInAnyOrder("Java Programming", "Spring Boot Development");
    }

    @Test
    void findByStudentId_withNonExistentStudent_shouldReturnEmptyList() {
        // Act
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByStudentId(999L);

        // Assert
        assertThat(enrollments).isEmpty();
    }

    @Test
    void findByCourseId_shouldReturnEnrollmentsForCourse() {
        // Act
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByCourseId(course1.getId());

        // Assert
        assertThat(enrollments).hasSize(2);
        assertThat(enrollments).extracting(e -> e.getStudent().getEmail())
                .containsExactlyInAnyOrder("student1@example.com", "student2@example.com");
    }

    @Test
    void findByCourseId_withNonExistentCourse_shouldReturnEmptyList() {
        // Act
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByCourseId(999L);

        // Assert
        assertThat(enrollments).isEmpty();
    }

    @Test
    void findByStudentIdAndCourseId_shouldReturnSpecificEnrollment() {
        // Act
        Optional<EnrollmentEntity> found = enrollmentRepository.findByStudentIdAndCourseId(student1.getId(), course1.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getStudent().getEmail()).isEqualTo("student1@example.com");
        assertThat(found.get().getCourse().getTitle()).isEqualTo("Java Programming");
    }

    @Test
    void findByStudentIdAndCourseId_withNonExistentCombination_shouldReturnEmpty() {
        // Act
        Optional<EnrollmentEntity> found = enrollmentRepository.findByStudentIdAndCourseId(student2.getId(), course2.getId());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void existsByStudentIdAndCourseId_shouldReturnTrueForExistingEnrollment() {
        // Act
        boolean exists = enrollmentRepository.existsByStudentIdAndCourseId(student1.getId(), course1.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByStudentIdAndCourseId_shouldReturnFalseForNonExistentEnrollment() {
        // Act
        boolean exists = enrollmentRepository.existsByStudentIdAndCourseId(student2.getId(), course2.getId());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void countByStudentId_shouldReturnCorrectCount() {
        // Act
        long count1 = enrollmentRepository.countByStudentId(student1.getId());
        long count2 = enrollmentRepository.countByStudentId(student2.getId());

        // Assert
        assertThat(count1).isEqualTo(2);
        assertThat(count2).isEqualTo(1);
    }

    @Test
    void countByStudentId_withNonExistentStudent_shouldReturnZero() {
        // Act
        long count = enrollmentRepository.countByStudentId(999L);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void countByCourseId_shouldReturnCorrectCount() {
        // Act
        long count1 = enrollmentRepository.countByCourseId(course1.getId());
        long count2 = enrollmentRepository.countByCourseId(course2.getId());

        // Assert
        assertThat(count1).isEqualTo(2);
        assertThat(count2).isEqualTo(1);
    }

    @Test
    void countByCourseId_withNonExistentCourse_shouldReturnZero() {
        // Act
        long count = enrollmentRepository.countByCourseId(999L);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void findByStudent_Email_shouldReturnEnrollmentsForStudentEmail() {
        // Act
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByStudent_Email("student1@example.com");

        // Assert
        assertThat(enrollments).hasSize(2);
        assertThat(enrollments).allMatch(e -> e.getStudent().getEmail().equals("student1@example.com"));
    }

    @Test
    void findByStudent_Email_withNonExistentEmail_shouldReturnEmptyList() {
        // Act
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByStudent_Email("nonexistent@example.com");

        // Assert
        assertThat(enrollments).isEmpty();
    }

    @Test
    void findByCourse_Title_shouldReturnEnrollmentsForCourseTitle() {
        // Act
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByCourse_Title("Java Programming");

        // Assert
        assertThat(enrollments).hasSize(2);
        assertThat(enrollments).allMatch(e -> e.getCourse().getTitle().equals("Java Programming"));
    }

    @Test
    void findByCourse_InstructorId_shouldReturnEnrollmentsForInstructor() {
        // Act
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByCourse_InstructorId(instructor.getId());

        // Assert
        assertThat(enrollments).hasSize(3);
        assertThat(enrollments).allMatch(e -> e.getCourse().getInstructor().getId().equals(instructor.getId()));
    }

    @Test
    void save_shouldPersistNewEnrollment() {
        // Arrange
        UserEntity newStudent = UserEntity.builder()
                .email("newstudent@example.com")
                .firstName("New")
                .lastName("Student")
                .password("password123")
                .roles(Set.of(studentRole))
                .createdAt(LocalDateTime.now())
                .build();
        newStudent = userRepository.save(newStudent);

        EnrollmentEntity newEnrollment = EnrollmentEntity.builder()
                .student(newStudent)
                .course(course2)
                .enrolledAt(LocalDateTime.now())
                .build();

        // Act
        EnrollmentEntity saved = enrollmentRepository.save(newEnrollment);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStudent().getEmail()).isEqualTo("newstudent@example.com");
        assertThat(saved.getCourse().getTitle()).isEqualTo("Spring Boot Development");
    }

    @Test
    void delete_shouldRemoveEnrollmentFromDatabase() {
        // Arrange
        Long enrollmentId = enrollment1.getId();

        // Act
        enrollmentRepository.delete(enrollment1);
        entityManager.flush();

        // Assert
        Optional<EnrollmentEntity> found = enrollmentRepository.findById(enrollmentId);
        assertThat(found).isEmpty();
    }

    @Test
    void findById_shouldReturnEnrollmentWithStudentAndCourseDetails() {
        // Act
        Optional<EnrollmentEntity> found = enrollmentRepository.findById(enrollment1.getId());

        // Assert
        assertThat(found).isPresent();
        EnrollmentEntity foundEnrollment = found.get();
        assertThat(foundEnrollment.getStudent()).isNotNull();
        assertThat(foundEnrollment.getCourse()).isNotNull();
        assertThat(foundEnrollment.getStudent().getFirstName()).isEqualTo("John");
        assertThat(foundEnrollment.getCourse().getTitle()).isEqualTo("Java Programming");
    }

    @Test
    void deleteByStudentIdAndCourseId_shouldRemoveSpecificEnrollment() {
        // Act
        enrollmentRepository.deleteByStudentIdAndCourseId(student1.getId(), course1.getId());
        entityManager.flush();

        // Assert
        Optional<EnrollmentEntity> found = enrollmentRepository.findByStudentIdAndCourseId(student1.getId(), course1.getId());
        assertThat(found).isEmpty();

        // Verify other enrollments still exist
        List<EnrollmentEntity> remainingEnrollments = enrollmentRepository.findByStudentId(student1.getId());
        assertThat(remainingEnrollments).hasSize(1);
    }
}