package com.rustem.eduthesis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rustem.eduthesis.api.controller.EnrollmentController;
import com.rustem.eduthesis.api.dto.EnrollmentResponse;
import com.rustem.eduthesis.api.dto.SimpleCourseDTO;
import com.rustem.eduthesis.api.dto.SimpleUserDTO;
import com.rustem.eduthesis.config.TestSecurityConfig;
import com.rustem.eduthesis.infrastructure.security.jwt.JwtTokenProvider;
import com.rustem.eduthesis.infrastructure.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
@Import(TestSecurityConfig.class)
public class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnrollmentService enrollmentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private EnrollmentResponse sampleEnrollment1;
    private EnrollmentResponse sampleEnrollment2;

    @BeforeEach
    void setUp() {
        sampleEnrollment1 = new EnrollmentResponse();
        sampleEnrollment1.setId(1L);
        sampleEnrollment1.setCourse(SimpleCourseDTO.builder().title("Java Programming").id(101L).build());
        sampleEnrollment1.setStudent(new SimpleUserDTO(
                201L, "John", "Doe", "johnDoe@mail.com"
        ));
        sampleEnrollment1.setEnrolledAt(LocalDateTime.now());
        sampleEnrollment1.setProgressPercentage(25d);

        sampleEnrollment2 = new EnrollmentResponse();
        sampleEnrollment2.setId(2L);
        sampleEnrollment2.setCourse(SimpleCourseDTO.builder().title("C++ Programming").id(102L).build());
        sampleEnrollment2.setStudent(new SimpleUserDTO(
                201L, "Sarah", "Connor", "sarahConnor@mail.com"
        ));
        sampleEnrollment2.setEnrolledAt(LocalDateTime.now());
        sampleEnrollment2.setProgressPercentage(50d);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void enrollCurrentUser_WithStudentRole_shouldReturnSuccessMessage() throws Exception {
        Long courseId = 101L;
        doNothing().when(enrollmentService).enrollCurrentUserInCourse(courseId);

        mockMvc.perform(post("/api/enrollment/courses/{courseId}", courseId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Successfully enrolled in course")));

        verify(enrollmentService).enrollCurrentUserInCourse(courseId);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void unenrollCurrentUserFromCourse_shouldReturnSuccessMessage() throws Exception {
        Long courseId = 101L;
        doNothing().when(enrollmentService).unenrollCurrentUserFromCourse(courseId);

        mockMvc.perform(delete("/api/enrollment/courses/{courseId}", courseId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Successfully unenrolled from course")));

        verify(enrollmentService).unenrollCurrentUserFromCourse(courseId);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getEnrollmentsForCurrentUser_shouldReturnListOfEnrollments() throws Exception {
        List<EnrollmentResponse> enrollments = Arrays.asList(sampleEnrollment1, sampleEnrollment2);
        when(enrollmentService.getEnrollmentsForCurrentUser()).thenReturn(enrollments);

        mockMvc.perform(get("/api/enrollment/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].course.id", is(101)))
                .andExpect(jsonPath("$[0].course.title", is("Java Programming")))
                .andExpect(jsonPath("$[1].course.id", is(102)))
                .andExpect(jsonPath("$[1].course.title", is("C++ Programming")));

        verify(enrollmentService).getEnrollmentsForCurrentUser();
    }

    @Test
    @WithMockUser
    void enrollInCourse_withoutStudentRole_shouldReturnForbidden() throws Exception {
        Long courseId = 101L;

        mockMvc.perform(post("/api/enrollment/courses/{courseId}", courseId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void unenrollFromCourse_withoutStudentRole_shouldReturnForbidden() throws Exception {
        Long courseId = 101L;

        mockMvc.perform(delete("/api/enrollment/courses/{courseId}", courseId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"INSTRUCTOR", "ADMIN"})
    void getMyEnrollments_withoutStudentRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/enrollment/courses"))
                .andExpect(status().isForbidden());

        verify(enrollmentService, never()).getEnrollmentsForCurrentUser();
    }
}
