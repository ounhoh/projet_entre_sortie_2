package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Clé primaire composite pour NotificationAgentDirectionEntity.
 */
public class NotificationAgentDirectionId implements Serializable {
    
    private UUID notificationId;
    private UUID agentDirectionId;
    
    public NotificationAgentDirectionId() {}
    
    public NotificationAgentDirectionId(UUID notificationId, UUID agentDirectionId) {
        this.notificationId = notificationId;
        this.agentDirectionId = agentDirectionId;
    }
    
    public UUID getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }
    
    public UUID getAgentDirectionId() {
        return agentDirectionId;
    }
    
    public void setAgentDirectionId(UUID agentDirectionId) {
        this.agentDirectionId = agentDirectionId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationAgentDirectionId that = (NotificationAgentDirectionId) o;
        return Objects.equals(notificationId, that.notificationId) &&
               Objects.equals(agentDirectionId, that.agentDirectionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(notificationId, agentDirectionId);
    }
}
