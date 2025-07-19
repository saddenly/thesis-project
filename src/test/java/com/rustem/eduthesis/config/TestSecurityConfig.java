package com.rustem.eduthesis.config;

import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;
import com.rustem.eduthesis.infrastructure.security.MyExpressionHandler;
import com.rustem.eduthesis.infrastructure.security.MySecurityExpressionRoot;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public CourseRepository courseRepository() {
        return mock(CourseRepository.class);
    }

    @Bean
    @Primary
    public UserRepository userRepository() {
        return mock(UserRepository.class);
    }

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    @Primary
    public MySecurityExpressionRoot mySecurityExpressionRoot() {
        MySecurityExpressionRoot mockRoot = mock(MySecurityExpressionRoot.class);
        when(mockRoot.isCourseOwnerOrAdmin(any(Long.class))).thenReturn(true);
        return mockRoot;
    }

    @Bean
    @Primary
    public MyExpressionHandler myExpressionHandler(CourseRepository courseRepository, UserRepository userRepository) {
        return new MyExpressionHandler(courseRepository, userRepository);
    }
}
