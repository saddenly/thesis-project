package com.rustem.eduthesis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rustem.eduthesis.api.controller.LessonController;
import com.rustem.eduthesis.api.dto.LessonRequest;
import com.rustem.eduthesis.api.dto.LessonResponse;
import com.rustem.eduthesis.api.dto.SimpleCourseDTO;
import com.rustem.eduthesis.config.TestSecurityConfig;
import com.rustem.eduthesis.infrastructure.security.jwt.JwtTokenProvider;
import com.rustem.eduthesis.infrastructure.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LessonController.class)
@Import(TestSecurityConfig.class)
public class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LessonService lessonService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private LessonResponse lessonResponse1;
    private LessonResponse lessonResponse2;
    private LessonRequest lessonRequest;

    @BeforeEach
    void setUp() {
        // Set up test data
        lessonResponse1 = new LessonResponse();
        lessonResponse1.setId(1L);
        lessonResponse1.setTitle("Introduction to Java");
        lessonResponse1.setContent("Java basics content");
        lessonResponse1.setCourse(SimpleCourseDTO.builder()
                        .id(1L)
                .build());
        lessonResponse1.setOrderIndex(1);
        lessonResponse1.setDurationMinutes(60);

        lessonResponse2 = new LessonResponse();
        lessonResponse2.setId(2L);
        lessonResponse2.setTitle("Java Variables");
        lessonResponse2.setContent("Variables content");
        lessonResponse2.setCourse(SimpleCourseDTO.builder()
                        .id(1L)
                .build());
        lessonResponse2.setOrderIndex(2);
        lessonResponse2.setDurationMinutes(45);

        lessonRequest = new LessonRequest();
        lessonRequest.setTitle("New Lesson");
        lessonRequest.setContent("Lesson content with sufficient length for validation");
        lessonRequest.setOrderIndex(3);
        lessonRequest.setDurationMinutes(30);
        lessonRequest.setVideoUrl("https://example.com/video.mp4");
        lessonRequest.setAdditionalResources("Additional materials for the lesson");
    }

    @Test
    @WithMockUser
    void getLessonsForCourse_shouldReturnLessons() throws Exception {
        // Arrange
        Long courseId = 1L;
        List<LessonResponse> lessons = Arrays.asList(lessonResponse1, lessonResponse2);
        when(lessonService.getLessonsForCourse(courseId)).thenReturn(lessons);

        // Act & Assert
        mockMvc.perform(get("/api/courses/{courseId}/lessons", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Introduction to Java")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Java Variables")));

        verify(lessonService).getLessonsForCourse(courseId);
    }

    @Test
    @WithMockUser
    void getLessonById_shouldReturnLesson() throws Exception {
        // Arrange
        Long courseId = 1L;
        Long lessonId = 1L;
        when(lessonService.getLessonById(courseId, lessonId)).thenReturn(lessonResponse1);

        // Act & Assert
        mockMvc.perform(get("/api/courses/{courseId}/lessons/{lessonId}", courseId, lessonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Introduction to Java")))
                .andExpect(jsonPath("$.content", is("Java basics content")));

        verify(lessonService).getLessonById(courseId, lessonId);
    }

    @Test
    @WithMockUser
    void createLesson_withoutInstructorRole_shouldReturnForbidden() throws Exception {
        // Act & Assert
        Long courseId = 1L;
        mockMvc.perform(post("/api/courses/{courseId}/lessons", courseId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonRequest)))
                .andExpect(status().isForbidden());

        verify(lessonService, never()).createLesson(anyLong(), any(LessonRequest.class));
    }
}
