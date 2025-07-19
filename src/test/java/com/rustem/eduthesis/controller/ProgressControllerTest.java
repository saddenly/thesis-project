package com.rustem.eduthesis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rustem.eduthesis.api.controller.ProgressController;
import com.rustem.eduthesis.api.dto.ProgressResponse;
import com.rustem.eduthesis.api.dto.SimpleCourseDTO;
import com.rustem.eduthesis.api.dto.SimpleLessonDTO;
import com.rustem.eduthesis.config.TestSecurityConfig;
import com.rustem.eduthesis.infrastructure.security.jwt.JwtTokenProvider;
import com.rustem.eduthesis.infrastructure.service.ProgressService;
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

@WebMvcTest(ProgressController.class)
@Import(TestSecurityConfig.class)
public class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ProgressService progressService;

    private ProgressResponse progressResponse1;
    private ProgressResponse progressResponse2;

    @BeforeEach
    void setUp() {
        progressResponse1 = new ProgressResponse();
        progressResponse1.setId(1L);
        progressResponse1.setCourse(SimpleCourseDTO.builder()
                        .id(101L)
                        .title("Java Programming")
                .build());
        progressResponse1.setLesson(SimpleLessonDTO.builder()
                        .id(201L)
                        .title("Introduction to Java")
                .build());
        progressResponse1.setCompleted(true);
        progressResponse1.setCompletedAt(LocalDateTime.now());

        progressResponse2 = new ProgressResponse();
        progressResponse2.setId(2L);
        progressResponse2.setCourse(SimpleCourseDTO.builder()
                .id(101L)
                .title("Java Programming")
                .build());
        progressResponse2.setLesson(SimpleLessonDTO.builder()
                .id(202L)
                .title("Variables and Data Types")
                .build());
        progressResponse2.setCompleted(false);
        progressResponse2.setCompletedAt(null);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void markLessonAsCompleted_shouldReturnSuccessMessage() throws Exception {
        Long lessonId = 201L;
        doNothing().when(progressService).markLessonAsCompleted(lessonId);

        mockMvc.perform(patch("/api/progress/lessons/{lessonId}/complete", lessonId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Lesson marked as completed")));

        verify(progressService).markLessonAsCompleted(lessonId);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getProgressForCourse_shouldReturnProgressList() throws Exception {
        Long courseId = 101L;
        List<ProgressResponse> progressList = Arrays.asList(progressResponse1, progressResponse2);

        when(progressService.getProgressForCourse(courseId)).thenReturn(progressList);

        mockMvc.perform(get("/api/progress/courses/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].lesson.title", is("Introduction to Java")))
                .andExpect(jsonPath("$[0].completed", is(true)))
                .andExpect(jsonPath("$[1].lesson.title", is("Variables and Data Types")))
                .andExpect(jsonPath("$[1].completed", is(false)));

        verify(progressService).getProgressForCourse(courseId);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getMyProgress_shouldReturnProgressSummary() throws Exception {
        List<ProgressResponse> progressList = Arrays.asList(progressResponse1, progressResponse2);

        when(progressService.getProgressForCurrentStudent()).thenReturn(progressList);

        mockMvc.perform(get("/api/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].course.title", is("Java Programming")))
                .andExpect(jsonPath("$[1].course.title", is("Java Programming")));

        verify(progressService).getProgressForCurrentStudent();
    }

    @Test
    @WithMockUser
    void markLessonAsCompleted_withoutStudentRole_shouldReturnForbidden() throws Exception {
        Long lessonId = 201L;

        mockMvc.perform(patch("/api/progress/lessons/{lessonId}/complete", lessonId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getProgressForCourse_withoutStudentRole_shouldReturnForbidden() throws Exception {
        Long courseId = 101L;

        mockMvc.perform(get("/api/progress/courses/{courseId}", courseId))
                .andExpect(status().isForbidden());

        verify(progressService, never()).getProgressForCourse(any());
    }

    @Test
    @WithMockUser
    void getMyProgress_withoutStudentRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/progress"))
                .andExpect(status().isForbidden());

        verify(progressService, never()).getProgressForCurrentStudent();
    }
}
