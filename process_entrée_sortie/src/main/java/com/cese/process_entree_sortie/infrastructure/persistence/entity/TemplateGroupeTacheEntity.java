package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "template_groupe_tache")
public class TemplateGroupeTacheEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "code_template", nullable = false)
    private String codeTemplate;
    
    @Column(name = "code_direction")
    private String codeDirection;
    
    @Column(name = "lib_group_tache", nullable = false)
    private String libGroupTache;
    
    @Column(name = "statut_processus_id", nullable = false, columnDefinition = "UUID")
    private UUID statutProcessusId;
    
    @Column(name = "direction_id", nullable = false, columnDefinition = "UUID")
    private UUID directionId;
    
    @Column(name = "template_processus_id", nullable = false, columnDefinition = "UUID")
    private UUID templateProcessusId;
    
    @Column(name = "is_direction_concernee", nullable = false, columnDefinition = "boolean default false")
    private boolean isDirectionConcernee;
    
    public TemplateGroupeTacheEntity() {}
    
    public TemplateGroupeTacheEntity(UUID id, String codeTemplate, String codeDirection,
                                     String libGroupTache, UUID statutProcessusId,
                                     UUID directionId, UUID templateProcessusId, boolean isDirectionConcernee) {
        this.id = id;
        this.codeTemplate = codeTemplate;
        this.codeDirection = codeDirection;
        this.libGroupTache = libGroupTache;
        this.statutProcessusId = statutProcessusId;
        this.directionId = directionId;
        this.templateProcessusId = templateProcessusId;
        this.isDirectionConcernee = isDirectionConcernee;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCodeTemplate() {
        return codeTemplate;
    }
    
    public void setCodeTemplate(String codeTemplate) {
        this.codeTemplate = codeTemplate;
    }
    
    public String getCodeDirection() {
        return codeDirection;
    }
    
    public void setCodeDirection(String codeDirection) {
        this.codeDirection = codeDirection;
    }
    
    public String getLibGroupTache() {
        return libGroupTache;
    }
    
    public void setLibGroupTache(String libGroupTache) {
        this.libGroupTache = libGroupTache;
    }
    
    public UUID getStatutProcessusId() {
        return statutProcessusId;
    }
    
    public void setStatutProcessusId(UUID statutProcessusId) {
        this.statutProcessusId = statutProcessusId;
    }
    
    public UUID getDirectionId() {
        return directionId;
    }
    
    public void setDirectionId(UUID directionId) {
        this.directionId = directionId;
    }
    
    public UUID getTemplateProcessusId() {
        return templateProcessusId;
    }
    
    public void setTemplateProcessusId(UUID templateProcessusId) {
        this.templateProcessusId = templateProcessusId;
    }
    
    public boolean isDirectionConcernee() {
        return isDirectionConcernee;
    }
    
    public void setDirectionConcernee(boolean directionConcernee) {
        isDirectionConcernee = directionConcernee;
    }
}