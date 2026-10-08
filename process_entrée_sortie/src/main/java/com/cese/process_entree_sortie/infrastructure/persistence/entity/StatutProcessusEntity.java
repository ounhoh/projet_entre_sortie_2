package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "statut_processus")
public class StatutProcessusEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "code_statut", nullable = false)
    private String codeStatut;
    
    @Column(name = "lib_statut", nullable = false)
    private String libStatut;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "ordre")
    private Integer ordre;
    
    @Column(name = "template_processus_id", columnDefinition = "UUID")
    private UUID templateProcessusId;
    
    public StatutProcessusEntity() {}
    
    public StatutProcessusEntity(UUID id, String codeStatut, String libStatut) {
        this.id = id;
        this.codeStatut = codeStatut;
        this.libStatut = libStatut;
    }
    
    public StatutProcessusEntity(UUID id, String codeStatut, String libStatut, String description) {
        this.id = id;
        this.codeStatut = codeStatut;
        this.libStatut = libStatut;
        this.description = description;
    }
    
    public StatutProcessusEntity(UUID id, String codeStatut, String libStatut, String description, Integer ordre, UUID templateProcessusId) {
        this.id = id;
        this.codeStatut = codeStatut;
        this.libStatut = libStatut;
        this.description = description;
        this.ordre = ordre;
        this.templateProcessusId = templateProcessusId;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCodeStatut() {
        return codeStatut;
    }
    
    public void setCodeStatut(String codeStatut) {
        this.codeStatut = codeStatut;
    }
    
    public String getLibStatut() {
        return libStatut;
    }
    
    public void setLibStatut(String libStatut) {
        this.libStatut = libStatut;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getOrdre() {
        return ordre;
    }
    
    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }
    
    public UUID getTemplateProcessusId() {
        return templateProcessusId;
    }
    
    public void setTemplateProcessusId(UUID templateProcessusId) {
        this.templateProcessusId = templateProcessusId;
    }
}