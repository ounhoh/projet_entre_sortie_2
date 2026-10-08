package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instance_processus")
public class InstanceProcessusEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "code_processus", nullable = false)
    private String codeProcessus;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_echeance", nullable = false)
    private LocalDate dateEcheance;
    
    @Column(name = "direction_concernee_id", nullable = false, columnDefinition = "UUID")
    private UUID directionConcerneeId;
    
    @Column(name = "agent_id", nullable = false, columnDefinition = "UUID")
    private UUID agentId;
    
    @Column(name = "template_id", nullable = false, columnDefinition = "UUID")
    private UUID templateId;
    
    @Column(name = "statut_id", nullable = false, columnDefinition = "UUID")
    private UUID statutId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_processus", nullable = false)
    private TypeProcessus typeProcessus;
    
    public InstanceProcessusEntity() {}
    
    public InstanceProcessusEntity(UUID id, String codeProcessus, LocalDateTime dateCreation,
                                   LocalDate dateEcheance, UUID directionConcerneeId, UUID agentId,
                                   UUID templateId, UUID statutId, TypeProcessus typeProcessus) {
        this.id = id;
        this.codeProcessus = codeProcessus;
        this.dateCreation = dateCreation;
        this.dateEcheance = dateEcheance;
        this.directionConcerneeId = directionConcerneeId;
        this.agentId = agentId;
        this.templateId = templateId;
        this.statutId = statutId;
        this.typeProcessus = typeProcessus;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCodeProcessus() {
        return codeProcessus;
    }
    
    public void setCodeProcessus(String codeProcessus) {
        this.codeProcessus = codeProcessus;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDate getDateEcheance() {
        return dateEcheance;
    }
    
    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }
    
    public UUID getDirectionConcerneeId() {
        return directionConcerneeId;
    }
    
    public void setDirectionConcerneeId(UUID directionConcerneeId) {
        this.directionConcerneeId = directionConcerneeId;
    }
    
    public UUID getAgentId() {
        return agentId;
    }
    
    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }
    
    public UUID getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }
    
    public UUID getStatutId() {
        return statutId;
    }
    
    public void setStatutId(UUID statutId) {
        this.statutId = statutId;
    }
    
    public TypeProcessus getTypeProcessus() {
        return typeProcessus;
    }
    
    public void setTypeProcessus(TypeProcessus typeProcessus) {
        this.typeProcessus = typeProcessus;
    }
}