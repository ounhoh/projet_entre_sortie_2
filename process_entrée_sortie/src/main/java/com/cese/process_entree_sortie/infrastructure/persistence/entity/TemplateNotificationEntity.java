package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import com.cese.process_entree_sortie.domain.utils.ValueObject.NiveauNotification;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "template_notification")
public class TemplateNotificationEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "niveau", nullable = false)
    private NiveauNotification niveau;
    
    @Column(name = "code_notification", nullable = false, unique = true)
    private String codeNotification;
    
    @Column(name = "object", nullable = false)
    private String object;
    
    @Column(name = "description", nullable = false, length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_envoyeur", nullable = false)
    private TacheType typeEnvoyeur;
    
    @Column(name = "tache_id", columnDefinition = "UUID")
    private UUID tacheId;
    
    @Column(name = "groupe_tache_id", columnDefinition = "UUID")
    private UUID groupeTacheId;
    
    public TemplateNotificationEntity() {}
    
    public TemplateNotificationEntity(UUID id, NiveauNotification niveau, String codeNotification,
                                     String object, String description, TacheType typeEnvoyeur,
                                     UUID tacheId, UUID groupeTacheId) {
        this.id = id;
        this.niveau = niveau;
        this.codeNotification = codeNotification;
        this.object = object;
        this.description = description;
        this.typeEnvoyeur = typeEnvoyeur;
        this.tacheId = tacheId;
        this.groupeTacheId = groupeTacheId;
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
    
    public String getCodeNotification() {
        return codeNotification;
    }
    
    public void setCodeNotification(String codeNotification) {
        this.codeNotification = codeNotification;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TacheType getTypeEnvoyeur() {
        return typeEnvoyeur;
    }
    
    public void setTypeEnvoyeur(TacheType typeEnvoyeur) {
        this.typeEnvoyeur = typeEnvoyeur;
    }
    
    public UUID getTacheId() {
        return tacheId;
    }
    
    public void setTacheId(UUID tacheId) {
        this.tacheId = tacheId;
    }
    
    public UUID getGroupeTacheId() {
        return groupeTacheId;
    }
    
    public void setGroupeTacheId(UUID groupeTacheId) {
        this.groupeTacheId = groupeTacheId;
    }
}