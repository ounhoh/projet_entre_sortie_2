package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Clé primaire composite pour TemplateGroupeTacheAssociationEntity.
 */
public class TemplateGroupeTacheAssociationId implements Serializable {
    
    private UUID templateGroupeId;
    private UUID templateTacheId;
    
    public TemplateGroupeTacheAssociationId() {}
    
    public TemplateGroupeTacheAssociationId(UUID templateGroupeId, UUID templateTacheId) {
        this.templateGroupeId = templateGroupeId;
        this.templateTacheId = templateTacheId;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateGroupeTacheAssociationId that = (TemplateGroupeTacheAssociationId) o;
        return Objects.equals(templateGroupeId, that.templateGroupeId) &&
               Objects.equals(templateTacheId, that.templateTacheId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(templateGroupeId, templateTacheId);
    }
}
