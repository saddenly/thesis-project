package com.rustem.eduthesis.infrastructure.repository;

import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    List<CourseEntity> findByPublishedTrue();

    List<CourseEntity> findByInstructorId(Long id);
}
