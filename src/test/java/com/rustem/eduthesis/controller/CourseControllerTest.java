package com.rustem.eduthesis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rustem.eduthesis.api.controller.CourseController;
import com.rustem.eduthesis.api.dto.CourseRequest;
import com.rustem.eduthesis.api.dto.CourseResponse;
import com.rustem.eduthesis.config.TestSecurityConfig;
import com.rustem.eduthesis.infrastructure.security.MyExpressionHandler;
import com.rustem.eduthesis.infrastructure.security.MySecurityExpressionRoot;
import com.rustem.eduthesis.infrastructure.security.jwt.JwtTokenProvider;
import com.rustem.eduthesis.infrastructure.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@Import({TestSecurityConfig.class})
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MySecurityExpressionRoot mySecurityExpressionRoot;

    @Autowired
    private MyExpressionHandler myExpressionHandler;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private CourseResponse sampleCourse1;
    private CourseResponse sampleCourse2;
    private CourseRequest courseRequest;

    @BeforeEach
    void setUp() {
        sampleCourse1 = new CourseResponse();
        sampleCourse1.setId(1L);
        sampleCourse1.setTitle("Java Programming");
        sampleCourse1.setDescription("Learn Java from scratch");

        sampleCourse2 = new CourseResponse();
        sampleCourse2.setId(2L);
        sampleCourse2.setTitle("Spring Boot");
        sampleCourse2.setDescription("Master Spring Boot framework");

        courseRequest = new CourseRequest();
        courseRequest.setTitle("New Course");
        courseRequest.setDescription("Course Description");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCourses_ShouldReturnAllCourses_WhenUserIsAdmin() throws Exception {
        List<CourseResponse> courses = List.of(sampleCourse1, sampleCourse2);
        when(courseService.getAllCourses()).thenReturn(courses);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Java Programming")))
                .andExpect(jsonPath("$[1].title", is("Spring Boot")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCourseById_ShouldReturnCourse_WhenUserIsAdmin() throws Exception {
        when(courseService.getCourseById(1L)).thenReturn(sampleCourse1);

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Java Programming")));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getPublishedCourses_ShouldReturnPublishedCourses_WhenUserIsStudent() throws Exception {
        List<CourseResponse> courses = List.of(sampleCourse1, sampleCourse2);
        when(courseService.getAllPublishedCourses()).thenReturn(courses);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Java Programming")))
                .andExpect(jsonPath("$[1].title", is("Spring Boot")));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getPublishedCourseById_ShouldReturnPublishedCourse_WhenUserIsStudent() throws Exception {
        when(courseService.getPublishedCourseById(1L)).thenReturn(sampleCourse1);

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Java Programming")));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createCourse_ShouldReturnCreatedCourse_WhenUserIsInstructor() throws Exception {
        when(courseService.createCourse(any(CourseRequest.class))).thenReturn(sampleCourse1);

        mockMvc.perform(post("/api/courses")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Java Programming")));

        verify(courseService).createCourse(any(CourseRequest.class));
    }

    @Test
    @WithMockUser
    void createCourse_withoutInstructorRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isForbidden());

        verify(courseService, never()).createCourse(any(CourseRequest.class));
    }
}
