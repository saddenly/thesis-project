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

    Boolean existsByStudentIdAndLessonId(Long studentId, Long lessonId);

    List<ProgressEntity> findByStudentId(Long studentId);

    List<ProgressEntity> findByLessonId(Long lessonId);

    List<ProgressEntity> findByStudentIdAndCompleted(Long studentId, Boolean completed);

    List<ProgressEntity> findByLesson_CourseId(Long courseId);

    List<ProgressEntity> findByStudentIdAndLesson_CourseId(Long studentId, Long courseId);

    List<ProgressEntity> findByStudent_Email(String email);

    List<ProgressEntity> findByLesson_Course_InstructorId(Long instructorId);

    List<ProgressEntity> findByCompletedTrue();

    List<ProgressEntity> findByCompletedFalse();

    Long countByStudentIdAndLesson_CourseId(Long studentId, Long courseId);

    Long countByStudentIdAndLesson_CourseIdAndCompleted(Long studentId, Long courseId, Boolean completed);

    Long countByLessonIdAndCompleted(Long lessonId, Boolean completed);

    Long countByStudentIdAndCourseIdAndCompletedTrue(Long studentId, Long courseId);

    void deleteByStudentIdAndLessonId(Long studentId, Long lessonId);

    @Modifying
    @Query("DELETE FROM ProgressEntity p WHERE p.student.id = :studentId AND p.course.id = :courseId")
    void deleteByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
