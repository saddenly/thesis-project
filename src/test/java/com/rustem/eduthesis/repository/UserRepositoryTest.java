package com.rustem.eduthesis.repository;

import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
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
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserEntity student;
    private UserEntity instructor;
    private UserEntity admin;
    private RoleEntity studentRole;
    private RoleEntity instructorRole;
    private RoleEntity adminRole;

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

        adminRole = RoleEntity.builder()
                .name("ADMIN")
                .build();
        adminRole = roleRepository.save(adminRole);

        student = UserEntity.builder()
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword123")
                .roles(Set.of(studentRole))
                .createdAt(LocalDateTime.now())
                .build();

        instructor = UserEntity.builder()
                .email("instructor@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("encodedPassword456")
                .roles(Set.of(instructorRole))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        admin = UserEntity.builder()
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .password("encodedPasswordAdmin")
                .roles(Set.of(adminRole))
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        student = userRepository.save(student);
        instructor = userRepository.save(instructor);
        admin = userRepository.save(admin);

        entityManager.flush();
    }

    @Test
    void findByEmail_shouldReturnUserWhenExists() {
        // Act
        Optional<UserEntity> found = userRepository.findByEmail("student@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
        assertThat(found.get().getRoles()).hasSize(1);
        assertThat(found.get().getRoles().iterator().next().getName()).isEqualTo("STUDENT");
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNotExists() {
        // Act
        Optional<UserEntity> found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_shouldBeCaseSensitive() {
        // Act
        Optional<UserEntity> found = userRepository.findByEmail("STUDENT@EXAMPLE.COM");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_shouldReturnTrueWhenExists() {
        // Act
        boolean exists = userRepository.existsByEmail("instructor@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalseWhenNotExists() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void findByRoles_Name_shouldReturnUsersWithSpecificRole() {
        // Act
        List<UserEntity> students = userRepository.findByRoles_Name("STUDENT");

        // Assert
        assertThat(students).hasSize(1);
        assertThat(students.get(0).getEmail()).isEqualTo("student@example.com");
    }

    @Test
    void findByRoles_Name_shouldReturnEmptyListForNonExistentRole() {
        // Act
        List<UserEntity> users = userRepository.findByRoles_Name("NONEXISTENT_ROLE");

        // Assert
        assertThat(users).isEmpty();
    }

    @Test
    void findByFirstNameContainingIgnoreCase_shouldReturnMatchingUsers() {
        // Act
        List<UserEntity> users = userRepository.findByFirstNameContainingIgnoreCase("john");

        // Assert
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void findByFirstNameContainingIgnoreCase_shouldBeCaseInsensitive() {
        // Act
        List<UserEntity> users = userRepository.findByFirstNameContainingIgnoreCase("JANE");

        // Assert
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getFirstName()).isEqualTo("Jane");
    }

    @Test
    void findByLastNameContainingIgnoreCase_shouldReturnMatchingUsers() {
        // Act
        List<UserEntity> users = userRepository.findByLastNameContainingIgnoreCase("doe");

        // Assert
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    void findByFirstNameAndLastName_shouldReturnExactMatch() {
        // Act
        List<UserEntity> found = userRepository.findByFirstNameAndLastName("John", "Doe");

        // Assert
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getEmail()).isEqualTo("student@example.com");
    }

    @Test
    void findByFirstNameAndLastName_shouldReturnEmptyForPartialMatch() {
        // Act
        List<UserEntity> found = userRepository.findByFirstNameAndLastName("John", "Smith");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void countByRoles_Name_shouldReturnCorrectCount() {
        // Act
        long studentCount = userRepository.countByRoles_Name("STUDENT");
        long instructorCount = userRepository.countByRoles_Name("INSTRUCTOR");
        long adminCount = userRepository.countByRoles_Name("ADMIN");

        // Assert
        assertThat(studentCount).isEqualTo(1);
        assertThat(instructorCount).isEqualTo(1);
        assertThat(adminCount).isEqualTo(1);
    }

    @Test
    void countByRoles_Name_shouldReturnZeroForNonExistentRole() {
        // Act
        long count = userRepository.countByRoles_Name("NONEXISTENT_ROLE");

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void findByEmailContainingIgnoreCase_shouldReturnMatchingUsers() {
        // Act
        List<UserEntity> users = userRepository.findByEmailContainingIgnoreCase("STUDENT");

        // Assert
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("student@example.com");
    }

    @Test
    void save_shouldPersistUserWithRoles() {
        // Arrange
        UserEntity newUser = UserEntity.builder()
                .email("newuser@example.com")
                .firstName("New")
                .lastName("User")
                .password("encodedPassword")
                .roles(Set.of(studentRole, instructorRole))
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        UserEntity saved = userRepository.save(newUser);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRoles()).hasSize(2);
        assertThat(saved.getRoles())
                .extracting(RoleEntity::getName)
                .containsExactlyInAnyOrder("STUDENT", "INSTRUCTOR");
    }

    @Test
    void delete_shouldRemoveUserFromDatabase() {
        // Arrange
        Long userId = student.getId();

        // Act
        userRepository.delete(student);
        entityManager.flush();

        // Assert
        Optional<UserEntity> found = userRepository.findById(userId);
        assertThat(found).isEmpty();
    }

    @Test
    void findById_shouldReturnUserWithLazyLoadedRoles() {
        // Act
        Optional<UserEntity> found = userRepository.findById(instructor.getId());

        // Assert
        assertThat(found).isPresent();
        UserEntity foundUser = found.get();
        assertThat(foundUser.getRoles()).isNotEmpty();
        assertThat(foundUser.getRoles().iterator().next().getName()).isEqualTo("INSTRUCTOR");
    }
}