package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import com.cese.process_entree_sortie.domain.utils.ValueObject.NiveauNotification;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "instance_notification")
public class InstanceNotificationEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "niveau", nullable = false)
    private NiveauNotification niveau;
    
    @Column(name = "date_envoie", nullable = false)
    private LocalDateTime dateEnvoie;
    
    @Column(name = "code_notification", nullable = false)
    private String codeNotification;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_envoyeur", nullable = false)
    private TacheType typeEnvoyeur;
    
    @Column(name = "template_notification_id", nullable = false, columnDefinition = "UUID")
    private UUID templateNotificationId;
    
    @Column(name = "groupe_tache_id", columnDefinition = "UUID")
    private UUID groupeTacheId;
    
    @Column(name = "tache_id", columnDefinition = "UUID")
    private UUID tacheId;
    
    @OneToMany(mappedBy = "notificationId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationAgentDirectionEntity> recus = new ArrayList<>();
    
    public InstanceNotificationEntity() {}
    
    public InstanceNotificationEntity(UUID id, NiveauNotification niveau, LocalDateTime dateEnvoie,
                                     String codeNotification, TacheType typeEnvoyeur, UUID templateNotificationId,
                                     UUID groupeTacheId, UUID tacheId) {
        this.id = id;
        this.niveau = niveau;
        this.dateEnvoie = dateEnvoie;
        this.codeNotification = codeNotification;
        this.typeEnvoyeur = typeEnvoyeur;
        this.templateNotificationId = templateNotificationId;
        this.groupeTacheId = groupeTacheId;
        this.tacheId = tacheId;
        this.recus = new ArrayList<>();
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public NiveauNotification getNiveau() {
        return niveau;
    }
    
    public void setNiveau(NiveauNotification niveau) {
        this.niveau = niveau;
    }
    
    public LocalDateTime getDateEnvoie() {
        return dateEnvoie;
    }
    
    public void setDateEnvoie(LocalDateTime dateEnvoie) {
        this.dateEnvoie = dateEnvoie;
    }
    
    public String getCodeNotification() {
        return codeNotification;
    }
    
    public void setCodeNotification(String codeNotification) {
        this.codeNotification = codeNotification;
    }
    
    public TacheType getTypeEnvoyeur() {
        return typeEnvoyeur;
    }
    
    public void setTypeEnvoyeur(TacheType typeEnvoyeur) {
        this.typeEnvoyeur = typeEnvoyeur;
    }
    
    public UUID getTemplateNotificationId() {
        return templateNotificationId;
    }
    
    public void setTemplateNotificationId(UUID templateNotificationId) {
        this.templateNotificationId = templateNotificationId;
    }
    
    public UUID getGroupeTacheId() {
        return groupeTacheId;
    }
    
    public void setGroupeTacheId(UUID groupeTacheId) {
        this.groupeTacheId = groupeTacheId;
    }
    
    public UUID getTacheId() {
        return tacheId;
    }
    
    public void setTacheId(UUID tacheId) {
        this.tacheId = tacheId;
    }
    
    public List<NotificationAgentDirectionEntity> getRecus() {
        return recus;
    }
    
    public void setRecus(List<NotificationAgentDirectionEntity> recus) {
        this.recus = recus != null ? new ArrayList<>(recus) : new ArrayList<>();
    }
}