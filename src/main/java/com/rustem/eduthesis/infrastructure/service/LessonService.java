package com.rustem.eduthesis.infrastructure.service;

import com.rustem.eduthesis.api.dto.LessonRequest;
import com.rustem.eduthesis.api.dto.LessonResponse;
import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.LessonEntity;
import com.rustem.eduthesis.infrastructure.exception.CourseNotFoundException;
import com.rustem.eduthesis.infrastructure.exception.LessonNotFoundException;
import com.rustem.eduthesis.infrastructure.mapper.LessonMapper;
import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepo;
    private final CourseRepository courseRepo;
    private final LessonMapper lessonMapper;

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsForCourse(Long courseId) {
        CourseEntity courseEntity = courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));

        List<LessonEntity> lessons = lessonRepo.findByCourseOrderByOrderIndexAsc(courseEntity);

        return lessons.stream()
                .map(lessonMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LessonResponse getLessonById(Long courseId, Long lessonId) {
        if (!courseRepo.existsById(courseId)) throw new CourseNotFoundException("Course not found with ID: " + courseId);

        LessonEntity lesson = lessonRepo.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found with ID: " + lessonId + " for Course ID: " + courseId));
        return lessonMapper.toResponse(lesson);
    }

    @Transactional
    public LessonResponse createLesson(Long courseId, LessonRequest lessonRequest) {
        CourseEntity courseEntity = courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));

        Integer maxOrderIndex = lessonRepo.findMaxOrderIndexByCourseId(courseId).orElse(0);

        Integer orderIndex = lessonRequest.getOrderIndex() != null
                ? lessonRequest.getOrderIndex()
                : maxOrderIndex + 1;

        LessonEntity lesson = lessonMapper.toEntity(lessonRequest);
        lesson.setCourse(courseEntity);
        lesson.setOrderIndex(orderIndex);

        LessonEntity savedLesson = lessonRepo.save(lesson);
        return lessonMapper.toResponse(savedLesson);
    }

    @Transactional
    public LessonResponse updateLesson(Long courseId, Long lessonId, LessonRequest lessonRequest) {
        if (!courseRepo.existsById(courseId)) throw new CourseNotFoundException("Course not found with ID: " + courseId);

        LessonEntity lesson = lessonRepo.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found with ID: " + lessonId + " for Course ID: " + courseId));

        lesson.setTitle(lessonRequest.getTitle());
        lesson.setContent(lessonRequest.getContent());
        lesson.setVideoUrl(lessonRequest.getVideoUrl());
        lesson.setDurationMinutes(lessonRequest.getDurationMinutes());
        lesson.setOrderIndex(lessonRequest.getOrderIndex() != null ? lessonRequest.getOrderIndex() : lesson.getOrderIndex());
        lesson.setAdditionalResources(lessonRequest.getAdditionalResources());

        LessonEntity updatedLesson = lessonRepo.save(lesson);
        return lessonMapper.toResponse(updatedLesson);
    }

    @Transactional
    public void deleteLesson(Long courseId, Long lessonId) {
        if (!courseRepo.existsById(courseId)) throw new CourseNotFoundException("Course not found with ID: " + courseId);

        LessonEntity lesson = lessonRepo.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found with ID: " + lessonId + " for Course ID: " + courseId));
        lessonRepo.delete(lesson);
    }
}
