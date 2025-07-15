package com.rustem.eduthesis.infrastructure.service;

import com.rustem.eduthesis.api.dto.CourseRequest;
import com.rustem.eduthesis.api.dto.CourseResponse;
import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.CourseNotFoundException;
import com.rustem.eduthesis.infrastructure.exception.UserNotFoundException;
import com.rustem.eduthesis.infrastructure.mapper.CourseMapper;
import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepo;
    private final UserRepository userRepo;
    private final CourseMapper courseMapper;

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepo.findAll().stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllPublishedCourses() {
        return courseRepo.findByPublishedTrue().stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getPublishedCourseById(Long id) {
        CourseEntity courseEntity = courseRepo.findByIdAndPublishedTrue(id)
                .orElseThrow(() -> new CourseNotFoundException("Published course not found with ID: " + id));
        return courseMapper.toResponse(courseEntity);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        CourseEntity courseEntity = courseRepo.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + id));
        return courseMapper.toResponse(courseEntity);
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest courseRequest) {
        UserEntity instructor = getCurrentUserEntity();

        CourseEntity courseEntity = CourseEntity.builder()
                .title(courseRequest.getTitle())
                .description(courseRequest.getDescription())
                .imageUrl(courseRequest.getImageUrl())
                .published(false)
                .instructor(instructor)
                .build();

        CourseEntity savedCourseEntity = courseRepo.save(courseEntity);

        CourseResponse courseResponse = courseMapper.toResponse(savedCourseEntity);
        courseResponse.setCreatedAt(savedCourseEntity.getCreatedAt());
        courseResponse.setUpdatedAt(savedCourseEntity.getUpdatedAt());

        return courseResponse;
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest courseRequest) {
        CourseEntity courseEntity = courseRepo.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + id));

        courseEntity.setTitle(courseRequest.getTitle());
        courseEntity.setDescription(courseRequest.getDescription());
        Optional.ofNullable(courseRequest.getImageUrl()).ifPresent(courseEntity::setImageUrl);

        CourseEntity updatedCourse = courseRepo.save(courseEntity);
        updatedCourse.setUpdatedAt(LocalDateTime.now());

        return courseMapper.toResponse(updatedCourse);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepo.existsById(id)) {
            throw new CourseNotFoundException("Course not found with ID: " + id);
        }
        courseRepo.deleteById(id);
    }

    @Transactional
    public void publishCourse(Long id) {
        CourseEntity course = courseRepo.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + id));

        course.setPublished(true);
        course.setPublishedAt(LocalDateTime.now());

        courseRepo.save(course);
    }

    private UserEntity getCurrentUserEntity() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userDetails.getUsername()));
    }
}
