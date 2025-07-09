package com.rustem.eduthesis.infrastructure.repository;

import com.rustem.eduthesis.infrastructure.entity.ProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<ProgressEntity, Long> {

    Optional<ProgressEntity> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    List<ProgressEntity> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<ProgressEntity> findByStudentId(Long studentId);

    Long countByStudentIdAndCourseIdAndCompletedTrue(Long studentId, Long courseId);

    @Modifying
    @Query("DELETE FROM ProgressEntity p WHERE p.student.id = :studentId AND p.course.id = :courseId")
    void deleteByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
