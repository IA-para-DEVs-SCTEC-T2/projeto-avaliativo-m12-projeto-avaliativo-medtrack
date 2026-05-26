package com.medtrack.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:medtrack@noreply.com}")
    private String fromEmail;

    public void sendDoseReminder(String toEmail, String medicationName, String dosage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("MedTrack — Lembrete de Medicamento");
            message.setText(buildDoseReminderBody(medicationName, dosage));
            mailSender.send(message);
            log.info("Lembrete enviado para {} — medicamento: {}", toEmail, medicationName);
        } catch (Exception e) {
            log.error("Erro ao enviar lembrete para {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendInteractionAlert(String toEmail, String medA, String medB, String severity, String description) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("MedTrack — ⚠️ Alerta de Interação Medicamentosa");
            message.setText(buildInteractionAlertBody(medA, medB, severity, description));
            mailSender.send(message);
            log.info("Alerta de interação enviado para {} — {} + {}", toEmail, medA, medB);
        } catch (Exception e) {
            log.error("Erro ao enviar alerta para {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildDoseReminderBody(String medicationName, String dosage) {
        return """
                Olá!
                
                Este é um lembrete do MedTrack para tomar seu medicamento:
                
                💊 Medicamento: %s
                📋 Dosagem: %s
                
                Lembre-se: este sistema NÃO substitui orientação médica profissional.
                
                — MedTrack
                """.formatted(medicationName, dosage != null ? dosage : "conforme prescrição");
    }

    private String buildInteractionAlertBody(String medA, String medB, String severity, String description) {
        return """
                ⚠️ ALERTA DE INTERAÇÃO MEDICAMENTOSA
                
                Foi detectada uma interação entre medicamentos na sua lista:
                
                💊 %s + %s
                🔴 Severidade: %s
                📋 Descrição: %s
                
                IMPORTANTE: Consulte seu médico ou farmacêutico antes de combinar estes medicamentos.
                
                Este sistema NÃO substitui orientação médica profissional.
                
                — MedTrack
                """.formatted(medA, medB, severity, description);
    }
}
