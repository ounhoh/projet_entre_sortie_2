package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité d'association entre TemplateGroupeTache et TemplateTache.
 * Table de jointure pour la relation many-to-many.
 */
@Entity
@Table(name = "template_groupe_association_tache")
@IdClass(TemplateGroupeTacheAssociationId.class)
public class TemplateGroupeTacheAssociationEntity {
    
    @Id
    @Column(name = "template_groupe_id", nullable = false, columnDefinition = "UUID")
    private UUID templateGroupeId;
    
    @Id
    @Column(name = "template_tache_id", nullable = false, columnDefinition = "UUID")
    private UUID templateTacheId;
    
    @Column(name = "ordre")
    private Integer ordre; // Ordre de la tâche dans le groupe (optionnel)
    
    public TemplateGroupeTacheAssociationEntity() {}
    
    public TemplateGroupeTacheAssociationEntity(UUID templateGroupeId, UUID templateTacheId, Integer ordre) {
        this.templateGroupeId = templateGroupeId;
        this.templateTacheId = templateTacheId;
        this.ordre = ordre;
    }
    
    public UUID getTemplateGroupeId() {
        return templateGroupeId;
    }
    
    public void setTemplateGroupeId(UUID templateGroupeId) {
        this.templateGroupeId = templateGroupeId;
    }
    
    public UUID getTemplateTacheId() {
        return templateTacheId;
    }
    
    public void setTemplateTacheId(UUID templateTacheId) {
        this.templateTacheId = templateTacheId;
    }
    
    public Integer getOrdre() {
        return ordre;
    }
    
    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }
}
