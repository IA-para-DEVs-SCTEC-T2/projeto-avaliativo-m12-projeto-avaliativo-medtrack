package com.medtrack.service;

import com.medtrack.dto.request.LoginRequest;
import com.medtrack.dto.request.RegisterRequest;
import com.medtrack.dto.response.AuthResponse;
import com.medtrack.model.User;
import com.medtrack.model.enums.Role;
import com.medtrack.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Testes Unitários")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register deve criar usuário e retornar token")
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("test@email.com", "password123");
        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateToken("test@email.com", "USER")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("test@email.com");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("register deve lançar exceção se email já existe")
    void register_shouldThrowIfEmailExists() {
        RegisterRequest request = new RegisterRequest("existing@email.com", "password");
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já cadastrado");
    }

    @Test
    @DisplayName("login deve retornar token para credenciais válidas")
    void login_shouldReturnTokenForValidCredentials() {
        LoginRequest request = new LoginRequest("user@email.com", "password");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@email.com")
                .passwordHash("hashed")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtService.generateToken("user@email.com", "USER")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("login deve lançar exceção para senha incorreta")
    void login_shouldThrowForWrongPassword() {
        LoginRequest request = new LoginRequest("user@email.com", "wrong");
        User user = User.builder().email("user@email.com").passwordHash("hashed").active(true).role(Role.USER).build();

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciais inválidas");
    }

    @Test
    @DisplayName("login deve lançar exceção para conta desativada")
    void login_shouldThrowForInactiveAccount() {
        LoginRequest request = new LoginRequest("user@email.com", "password");
        User user = User.builder().email("user@email.com").passwordHash("hashed").active(false).role(Role.USER).build();

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desativada");
    }
}
