package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import com.cese.process_entree_sortie.domain.utils.ValueObject.ModeLecture;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "notification_agent_direction")
@IdClass(NotificationAgentDirectionId.class)
public class NotificationAgentDirectionEntity {
    
    @Id
    @Column(name = "notification_id", nullable = false, columnDefinition = "UUID")
    private UUID notificationId;
    
    @Id
    @Column(name = "agent_direction_id", nullable = false, columnDefinition = "UUID")
    private UUID agentDirectionId;
    
    @Column(name = "statut_lecture", nullable = false)
    private Boolean statutLecture;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_lecture")
    private ModeLecture modeLecture;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", insertable = false, updatable = false)
    private InstanceNotificationEntity notification;
    
    public NotificationAgentDirectionEntity() {}
    
    public NotificationAgentDirectionEntity(UUID notificationId, UUID agentDirectionId,
                                           Boolean statutLecture, ModeLecture modeLecture) {
        this.notificationId = notificationId;
        this.agentDirectionId = agentDirectionId;
        this.statutLecture = statutLecture;
        this.modeLecture = modeLecture;
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
    
    public Boolean getStatutLecture() {
        return statutLecture;
    }
    
    public void setStatutLecture(Boolean statutLecture) {
        this.statutLecture = statutLecture;
    }
    
    public ModeLecture getModeLecture() {
        return modeLecture;
    }
    
    public void setModeLecture(ModeLecture modeLecture) {
        this.modeLecture = modeLecture;
    }
    
    public InstanceNotificationEntity getNotification() {
        return notification;
    }
    
    public void setNotification(InstanceNotificationEntity notification) {
        this.notification = notification;
    }
}