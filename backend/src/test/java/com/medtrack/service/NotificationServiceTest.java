package com.medtrack.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService — Testes Unitários")
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("sendDoseReminder deve enviar e-mail com dados corretos")
    void sendDoseReminder_shouldSendEmail() {
        notificationService.sendDoseReminder("user@test.com", "Varfarina", "5mg");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).contains("user@test.com");
        assertThat(sent.getSubject()).contains("Lembrete");
        assertThat(sent.getText()).contains("Varfarina");
        assertThat(sent.getText()).contains("5mg");
    }

    @Test
    @DisplayName("sendDoseReminder deve usar texto padrão quando dosagem é null")
    void sendDoseReminder_shouldHandleNullDosage() {
        notificationService.sendDoseReminder("user@test.com", "Aspirina", null);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getText()).contains("conforme prescrição");
    }

    @Test
    @DisplayName("sendInteractionAlert deve enviar alerta com severidade")
    void sendInteractionAlert_shouldSendAlert() {
        notificationService.sendInteractionAlert(
                "user@test.com", "Varfarina", "Aspirina", "SEVERE", "Risco de sangramento");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getSubject()).contains("Interação");
        assertThat(sent.getText()).contains("Varfarina");
        assertThat(sent.getText()).contains("Aspirina");
        assertThat(sent.getText()).contains("SEVERE");
    }

    @Test
    @DisplayName("sendDoseReminder não deve propagar exceção do mailSender")
    void sendDoseReminder_shouldNotPropagateException() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        // Não deve lançar exceção
        notificationService.sendDoseReminder("user@test.com", "Varfarina", "5mg");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
