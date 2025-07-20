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
public class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    private RoleEntity studentRole;
    private RoleEntity instructorRole;
    private RoleEntity adminRole;
    private UserEntity student;
    private UserEntity instructor;

    @BeforeEach
    void setUp() {
        studentRole = RoleEntity.builder()
                .name("STUDENT")
                .build();

        instructorRole = RoleEntity.builder()
                .name("INSTRUCTOR")
                .build();

        adminRole = RoleEntity.builder()
                .name("ADMIN")
                .build();

        studentRole = roleRepository.save(studentRole);
        instructorRole = roleRepository.save(instructorRole);
        adminRole = roleRepository.save(adminRole);

        student = UserEntity.builder()
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .roles(Set.of(studentRole))
                .createdAt(LocalDateTime.now())
                .build();

        instructor = UserEntity.builder()
                .email("instructor@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("encodedPassword")
                .roles(Set.of(instructorRole, adminRole))
                .createdAt(LocalDateTime.now())
                .build();

        student = userRepository.save(student);
        instructor = userRepository.save(instructor);

        entityManager.flush();
    }

    @Test
    void findByName_shouldReturnRoleWhenExists() {
        // Act
        Optional<RoleEntity> found = roleRepository.findByName("STUDENT");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("STUDENT");
        assertThat(found.get().getId()).isNotNull();
    }

    @Test
    void findByName_shouldReturnEmptyWhenNotExists() {
        // Act
        Optional<RoleEntity> found = roleRepository.findByName("NONEXISTENT_ROLE");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findByName_shouldBeCaseSensitive() {
        // Act
        Optional<RoleEntity> found = roleRepository.findByName("student");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void existsByName_shouldReturnTrueWhenExists() {
        // Act
        boolean exists = roleRepository.existsByName("INSTRUCTOR");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_shouldReturnFalseWhenNotExists() {
        // Act
        boolean exists = roleRepository.existsByName("NONEXISTENT_ROLE");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void existsByName_shouldBeCaseSensitive() {
        // Act
        boolean exists = roleRepository.existsByName("admin");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void findAll_shouldReturnAllRoles() {
        // Act
        List<RoleEntity> roles = roleRepository.findAll();

        // Assert
        assertThat(roles).hasSize(3);
        assertThat(roles).extracting(RoleEntity::getName)
                .containsExactlyInAnyOrder("STUDENT", "INSTRUCTOR", "ADMIN");
    }

    @Test
    void findByNameIn_shouldReturnMatchingRoles() {
        // Arrange
        List<String> roleNames = List.of("STUDENT", "ADMIN", "NONEXISTENT");

        // Act
        List<RoleEntity> roles = roleRepository.findByNameIn(roleNames);

        // Assert
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(RoleEntity::getName)
                .containsExactlyInAnyOrder("STUDENT", "ADMIN");
    }

    @Test
    void findByNameIn_withEmptyList_shouldReturnEmptyList() {
        // Act
        List<RoleEntity> roles = roleRepository.findByNameIn(List.of());

        // Assert
        assertThat(roles).isEmpty();
    }

    @Test
    void findByUsers_Id_shouldReturnRolesForUser() {
        // Act
        List<RoleEntity> rolesForInstructor = roleRepository.findByUsers_Id(instructor.getId());

        // Assert
        assertThat(rolesForInstructor).hasSize(2);
        assertThat(rolesForInstructor).extracting(RoleEntity::getName)
                .containsExactlyInAnyOrder("INSTRUCTOR", "ADMIN");
    }

    @Test
    void findByUsers_Id_shouldReturnSingleRoleForStudent() {
        // Act
        List<RoleEntity> rolesForStudent = roleRepository.findByUsers_Id(student.getId());

        // Assert
        assertThat(rolesForStudent).hasSize(1);
        assertThat(rolesForStudent.get(0).getName()).isEqualTo("STUDENT");
    }

    @Test
    void findByUsers_Id_withNonExistentUser_shouldReturnEmptyList() {
        // Act
        List<RoleEntity> roles = roleRepository.findByUsers_Id(999L);

        // Assert
        assertThat(roles).isEmpty();
    }

    @Test
    void countByUsers_Id_shouldReturnCorrectCount() {
        // Act
        long countForInstructor = roleRepository.countByUsers_Id(instructor.getId());
        long countForStudent = roleRepository.countByUsers_Id(student.getId());

        // Assert
        assertThat(countForInstructor).isEqualTo(2);
        assertThat(countForStudent).isEqualTo(1);
    }

    @Test
    void countByUsers_Id_withNonExistentUser_shouldReturnZero() {
        // Act
        long count = roleRepository.countByUsers_Id(999L);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void save_shouldPersistNewRole() {
        // Arrange
        RoleEntity newRole = RoleEntity.builder()
                .name("MODERATOR")
                .build();

        // Act
        RoleEntity saved = roleRepository.save(newRole);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("MODERATOR");

        Optional<RoleEntity> found = roleRepository.findByName("MODERATOR");
        assertThat(found).isPresent();
    }

    @Test
    void delete_shouldRemoveRoleFromDatabase() {
        // Arrange
        RoleEntity roleToDelete = RoleEntity.builder()
                .name("TEMPORARY_ROLE")
                .build();
        roleToDelete = roleRepository.save(roleToDelete);
        Long roleId = roleToDelete.getId();

        // Act
        roleRepository.delete(roleToDelete);
        entityManager.flush();

        // Assert
        Optional<RoleEntity> found = roleRepository.findById(roleId);
        assertThat(found).isEmpty();
    }

    @Test
    void findById_shouldReturnRoleWithId() {
        // Act
        Optional<RoleEntity> found = roleRepository.findById(studentRole.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("STUDENT");
        assertThat(found.get().getId()).isEqualTo(studentRole.getId());
    }

    @Test
    void findById_withNonExistentId_shouldReturnEmpty() {
        // Act
        Optional<RoleEntity> found = roleRepository.findById(999L);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_shouldReturnMatchingRoles() {
        // Act
        List<RoleEntity> roles = roleRepository.findByNameContainingIgnoreCase("admin");

        // Assert
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getName()).isEqualTo("ADMIN");
    }

    @Test
    void findByNameContainingIgnoreCase_shouldBeCaseInsensitive() {
        // Act
        List<RoleEntity> roles = roleRepository.findByNameContainingIgnoreCase("STUD");

        // Assert
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getName()).isEqualTo("STUDENT");
    }

    @Test
    void findByNameContainingIgnoreCase_withNoMatches_shouldReturnEmptyList() {
        // Act
        List<RoleEntity> roles = roleRepository.findByNameContainingIgnoreCase("NONEXISTENT");

        // Assert
        assertThat(roles).isEmpty();
    }

    @Test
    void count_shouldReturnTotalNumberOfRoles() {
        // Act
        long count = roleRepository.count();

        // Assert
        assertThat(count).isEqualTo(3);
    }
}