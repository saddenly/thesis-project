package com.rustem.eduthesis.infrastructure.repository;

import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<LessonEntity, Long> {

    List<LessonEntity> findByCourse(CourseEntity course);

    List<LessonEntity> findByCourseOrderByOrderIndexAsc(CourseEntity course);

    List<LessonEntity> findByCourseIdOrderByOrderIndex(Long courseId);

    Optional<LessonEntity> findByIdAndCourseId(Long id, Long courseId);
}
