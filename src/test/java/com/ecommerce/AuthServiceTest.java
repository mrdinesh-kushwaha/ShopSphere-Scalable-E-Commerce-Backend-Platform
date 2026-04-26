package com.ecommerce;

import com.ecommerce.dto.request.AuthRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtils;
import com.ecommerce.service.impl.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private AuthRequest.Register registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new AuthRequest.Register();
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Test@1234");
    }

    @Test
    void register_ShouldSucceed_WhenEmailNotTaken() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var mockUserDetails = mock(org.springframework.security.core.userdetails.UserDetails.class);
        //when(mockUserDetails.getUsername()).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername(any())).thenReturn(mockUserDetails);
        when(jwtUtils.generateToken(any())).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("refresh-token");

        ApiResponse.AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrow_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(ApiException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() {
        AuthRequest.Login loginRequest = new AuthRequest.Login();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Test@1234");

        User user = User.builder()
                .id(1L).email("test@example.com")
                .firstName("Test").lastName("User")
                .role(User.Role.ROLE_USER).build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        var mockUserDetails = mock(org.springframework.security.core.userdetails.UserDetails.class);
        when(userDetailsService.loadUserByUsername(any())).thenReturn(mockUserDetails);
        when(jwtUtils.generateToken(any())).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("refresh-token");

        ApiResponse.AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType());
    }
}
