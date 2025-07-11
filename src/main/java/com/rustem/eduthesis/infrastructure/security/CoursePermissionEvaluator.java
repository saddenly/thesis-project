package com.rustem.eduthesis.infrastructure.security;

import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("coursePermissionEvaluator")
@RequiredArgsConstructor
public class CoursePermissionEvaluator {

    private final CourseRepository courseRepo;
    private final UserRepository userRepo;

    public boolean isCourseOwnerOrAdmin(Long courseId, UserDetails userDetails) {
        String email = userDetails.getUsername();

        // Check if the user is an admin
        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Check if the user is the course owner (instructor)
        return userRepo.findByEmail(email)
                .map(user -> courseRepo.findById(courseId)
                        .map(course -> course.getInstructor() != null && course.getInstructor().getId().equals(user.getId()))
                        .orElse(false))
                .orElse(false);
    }
}
