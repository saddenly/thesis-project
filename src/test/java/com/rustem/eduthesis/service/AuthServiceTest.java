package com.rustem.eduthesis.service;

import com.rustem.eduthesis.api.dto.SignupRequest;
import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.EmailAlreadyExistsException;
import com.rustem.eduthesis.infrastructure.exception.UserNotFoundException;
import com.rustem.eduthesis.infrastructure.repository.RoleRepository;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;
import com.rustem.eduthesis.infrastructure.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authService;

    private SignupRequest signupRequest;
    private RoleEntity studentRole;

    @BeforeEach
    void setUp() {
        // Set up test data
        signupRequest = new SignupRequest();
        signupRequest.setFirstName("Test User");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");

        studentRole = new RoleEntity();
        studentRole.setId(1L);
        studentRole.setName("STUDENT");

        RoleEntity instructorRole = new RoleEntity();
        instructorRole.setId(2L);
        instructorRole.setName("INSTRUCTOR");
    }

    @Test
    void registerUser_withValidRequestAndStudentRole_shouldRegisterUser() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        authService.registerUser(signupRequest);

        // Assert
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        UserEntity capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getFirstName()).isEqualTo("Test User");
        assertThat(capturedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(capturedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(capturedUser.getRoles()).hasSize(1);
        assertThat(capturedUser.getRoles().iterator().next().getName()).isEqualTo("STUDENT");
    }

    @Test
    void registerUser_withExistingEmail_shouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.registerUser(signupRequest));

        verify(userRepository, never()).save(any());
    }

    @Test
    void getCurrentUser_shouldReturnCurrentAuthenticatedUser() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("test@example.com");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Set security context
        SecurityContextHolder.setContext(securityContext);

        // Act
        UserEntity result = authService.getCurrentUser();

        // Assert
        assertThat(result).isEqualTo(user);

        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_withUserNotFoundInDatabase_shouldThrowException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> authService.getCurrentUser());

        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerUser_shouldSetCreatedAtTimestamp() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        authService.registerUser(signupRequest);

        // Assert
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        UserEntity capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getCreatedAt()).isNotNull();
    }
}
