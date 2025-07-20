package com.rustem.eduthesis.infrastructure.repository;

import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<LessonEntity, Long> {

    List<LessonEntity> findByCourseId(Long courseId);

    List<LessonEntity> findByCourseOrderByOrderIndexAsc(CourseEntity course);

    List<LessonEntity> findByCourseIdOrderByOrderIndex(Long courseId);

    Optional<LessonEntity> findByIdAndCourseId(Long id, Long courseId);

    @Query("SELECT MAX(l.orderIndex) FROM LessonEntity l WHERE l.course.id = :courseId")
    Optional<Integer> findMaxOrderIndexByCourseId(Long courseId);

    Boolean existsByIdAndCourseId(Long id, Long courseId);

    Boolean existsByTitleAndCourseId(String title, Long courseId);

    List<LessonEntity> findByTitleContainingIgnoreCase(String title);

    Optional<LessonEntity> findByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);

    List<LessonEntity> findByContentContainingIgnoreCase(String content);

    List<LessonEntity> findByOrderIndexGreaterThan(Integer orderIndex);

    Boolean existsByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);

    Long countByCourseId(Long courseId);

    List<LessonEntity> findByCourse_InstructorId(Long instructorId);

    List<LessonEntity> findByVideoUrlIsNotNull();

    List<LessonEntity> findByVideoUrlIsNull();
}
