package com.rustem.eduthesis.infrastructure.security;

import com.rustem.eduthesis.infrastructure.entity.CourseEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

public class MySecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final MethodSecurityExpressionOperations delegate;

    public MySecurityExpressionRoot(MethodSecurityExpressionOperations delegate, UserRepository userRepo, CourseRepository courseRepo) {
        this.delegate = delegate;
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
    }

    @Bean
    public boolean isCourseOwnerOrAdmin(Long courseId) {
        User user = (User) delegate.getAuthentication().getPrincipal();
        CourseEntity course = courseRepo.findById(courseId).orElse(null);
        UserEntity userEntity = userRepo.findByEmail(user.getUsername()).orElse(null);
        if (course == null || userEntity == null) {
            return false; // Course or user are not found
        }
        // Check if the user is the course owner (instructor) or admin
        return course.getInstructor() != null && course.getInstructor().getId().equals(userEntity.getId())
                || user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }


    @Override
    public void setFilterObject(Object filterObject) {
        delegate.setFilterObject(filterObject);
    }

    @Override
    public Object getFilterObject() {
        return delegate.getFilterObject();
    }

    @Override
    public void setReturnObject(Object returnObject) {
        delegate.setReturnObject(returnObject);
    }

    @Override
    public Object getReturnObject() {
        return delegate.getReturnObject();
    }

    @Override
    public Object getThis() {
        return delegate.getThis();
    }

    @Override
    public Authentication getAuthentication() {
        return delegate.getAuthentication();
    }

    @Override
    public boolean hasAuthority(String authority) {
        return delegate.hasAuthority(authority);
    }

    @Override
    public boolean hasAnyAuthority(String... authorities) {
        return delegate.hasAnyAuthority(authorities);
    }

    @Override
    public boolean hasRole(String role) {
        return delegate.hasRole(role);
    }

    @Override
    public boolean hasAnyRole(String... roles) {
        return delegate.hasAnyRole(roles);
    }

    @Override
    public boolean permitAll() {
        return delegate.permitAll();
    }

    @Override
    public boolean denyAll() {
        return delegate.denyAll();
    }

    @Override
    public boolean isAnonymous() {
        return delegate.isAnonymous();
    }

    @Override
    public boolean isAuthenticated() {
        return delegate.isAuthenticated();
    }

    @Override
    public boolean isRememberMe() {
        return delegate.isRememberMe();
    }

    @Override
    public boolean isFullyAuthenticated() {
        return delegate.isFullyAuthenticated();
    }

    @Override
    public boolean hasPermission(Object target, Object permission) {
        return delegate.hasPermission(target, permission);
    }

    @Override
    public boolean hasPermission(Object targetId, String targetType, Object permission) {
        return delegate.hasPermission(targetId, targetType, permission);
    }
}
