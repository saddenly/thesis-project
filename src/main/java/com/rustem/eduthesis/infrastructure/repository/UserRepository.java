package com.rustem.eduthesis.infrastructure.repository;

import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntity> findByRoles_Name(String roleName);

    List<UserEntity> findByFirstNameContainingIgnoreCase(String firstName);

    List<UserEntity> findByLastNameContainingIgnoreCase(String lastName);

    List<UserEntity> findByFirstNameAndLastName(String firstName, String lastName);

    long countByRoles_Name(String roleName);

    List<UserEntity> findByEmailContainingIgnoreCase(String email);
}
