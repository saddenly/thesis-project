package com.rustem.eduthesis.infrastructure.repository;

import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);

    List<RoleEntity> findByNameIn(List<String> names);

    List<RoleEntity> findByUsers_Id(Long userId);

    long countByUsers_Id(Long userId);

    List<RoleEntity> findByNameContainingIgnoreCase(String nameSubstring);
}
