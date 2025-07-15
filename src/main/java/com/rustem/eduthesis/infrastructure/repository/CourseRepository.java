package com.rustem.eduthesis.infrastructure.repository;

import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    List<CourseEntity> findByPublishedTrue();

    Optional<CourseEntity> findByIdAndPublishedTrue(Long id);

    List<CourseEntity> findByInstructorId(Long id);
}
