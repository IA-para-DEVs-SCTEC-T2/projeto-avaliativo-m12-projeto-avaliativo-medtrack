package com.medtrack.config;

import com.medtrack.model.User;
import com.medtrack.model.enums.Role;
import com.medtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria um usuário ADMIN inicial no primeiro start, caso ainda não exista nenhum no banco.
 *
 * <p>Idempotente: apenas executa o INSERT se nenhum usuário com {@link Role#ADMIN} estiver presente.
 * Lê e-mail e senha das variáveis {@code ADMIN_DEFAULT_EMAIL} e {@code ADMIN_DEFAULT_PASSWORD}.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-email:admin@medtrack.local}")
    private String defaultEmail;

    @Value("${app.admin.default-password:}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        boolean hasAdmin = userRepository.findAll().stream()
                .anyMatch(user -> user.getRole() == Role.ADMIN);

        if (hasAdmin) {
            log.debug("Admin já existe no banco — bootstrap ignorado");
            return;
        }

        if (defaultPassword == null || defaultPassword.isBlank()) {
            log.warn("ADMIN_DEFAULT_PASSWORD não definida; admin inicial NÃO foi criado. "
                    + "Defina a variável de ambiente para habilitar o bootstrap.");
            return;
        }

        User admin = User.builder()
                .email(defaultEmail)
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);

        log.info("Admin inicial criado: {} (troque a senha após o primeiro login)", defaultEmail);
    }
}
