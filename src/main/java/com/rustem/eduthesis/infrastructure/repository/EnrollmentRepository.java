package com.rustem.eduthesis.infrastructure.repository;

import com.rustem.eduthesis.infrastructure.entity.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<EnrollmentEntity> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<EnrollmentEntity> findByStudentId(Long studentId);

    List<EnrollmentEntity> findByCourseId(Long courseId);

    long countByStudentId(Long studentId);

    long countByCourseId(Long courseId);

    List<EnrollmentEntity> findByStudent_Email(String email);

    List<EnrollmentEntity> findByCourse_Title(String title);

    List<EnrollmentEntity> findByCourse_InstructorId(Long instructorId);

    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
}
