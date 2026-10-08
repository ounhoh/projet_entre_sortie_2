package com.cese.process_entree_sortie.application.port.in.notification;

import java.util.UUID;

public interface MarkAllNotificationReadApi {
    void markAllNotificationRead(UUID agentID);
}
