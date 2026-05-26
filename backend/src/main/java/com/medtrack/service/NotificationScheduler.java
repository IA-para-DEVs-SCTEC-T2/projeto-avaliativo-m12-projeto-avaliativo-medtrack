package com.medtrack.service;

import com.medtrack.model.UserMedication;
import com.medtrack.repository.SystemConfigRepository;
import com.medtrack.repository.UserMedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final UserMedicationRepository userMedicationRepository;
    private final NotificationService notificationService;
    private final SystemConfigRepository systemConfigRepository;

    /**
     * Executa a cada minuto para verificar lembretes pendentes.
     * Verifica se o horário atual (HH:mm) coincide com o reminder_time de algum medicamento.
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendReminders() {
        if (!isNotificationEnabled()) {
            return;
        }

        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        List<UserMedication> allMedications = userMedicationRepository.findAll();

        List<UserMedication> dueReminders = allMedications.stream()
                .filter(um -> um.getReminderTime() != null)
                .filter(um -> um.getReminderTime().getHour() == now.getHour()
                        && um.getReminderTime().getMinute() == now.getMinute())
                .filter(um -> um.getUser().getActive())
                .toList();

        if (!dueReminders.isEmpty()) {
            log.info("Encontrados {} lembretes para enviar às {}", dueReminders.size(), now);
        }

        for (UserMedication um : dueReminders) {
            notificationService.sendDoseReminder(
                    um.getUser().getEmail(),
                    um.getMedication().getName(),
                    um.getDosage()
            );
        }
    }

    private boolean isNotificationEnabled() {
        return systemConfigRepository.findByKey("NOTIFICATION_ENABLED")
                .map(config -> "true".equalsIgnoreCase(config.getValue()))
                .orElse(true);
    }
}
