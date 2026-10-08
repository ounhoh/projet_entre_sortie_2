package com.cese.process_entree_sortie.application.dto.notification.entree;

import java.time.LocalDate;
import java.util.UUID;

public record SendReminderCommand(UUID notificationId, LocalDate dateReminder) {
}
