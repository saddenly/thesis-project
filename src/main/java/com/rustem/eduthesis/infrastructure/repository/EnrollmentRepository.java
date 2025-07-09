package com.rustem.eduthesis.infrastructure.repository;

import com.rustem.eduthesis.infrastructure.entity.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<EnrollmentEntity> findByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<EnrollmentEntity> findByStudentId(Long studentId);

    Optional<EnrollmentEntity> findByCourseId(Long courseId);
}
