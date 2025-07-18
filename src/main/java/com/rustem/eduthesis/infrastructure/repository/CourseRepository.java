package com.rustem.eduthesis.infrastructure.repository;

import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    List<CourseEntity> findByPublishedTrue();

    Optional<CourseEntity> findByIdAndPublishedTrue(Long id);

    @Query("SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.course.id = :courseId")
    int countEnrollmentsByCourseId(@Param("courseId") Long courseId);

    List<CourseEntity> findByInstructorId(Long id);
}
