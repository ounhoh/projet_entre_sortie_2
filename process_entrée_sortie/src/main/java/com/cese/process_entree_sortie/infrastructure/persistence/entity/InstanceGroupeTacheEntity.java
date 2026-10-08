package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "instance_groupe_tache")
public class InstanceGroupeTacheEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "date_echeance", nullable = false)
    private LocalDate dateEcheance;
    
    @Column(name = "code", nullable = false)
    private String code;
    
    @Column(name = "libelle")
    private String libelle;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutTache statut;
    
    @Column(name = "template_id", nullable = false, columnDefinition = "UUID")
    private UUID templateId;
    
    @Column(name = "processus_id", nullable = false, columnDefinition = "UUID")
    private UUID processusId;
    
    @ElementCollection
    @CollectionTable(name = "tache_agent_assignation", joinColumns = @JoinColumn(name = "groupe_tache_id"))
    @Column(name = "agent_id", columnDefinition = "UUID")
    private List<UUID> agentAssigneIdList = new ArrayList<>();
    
    public InstanceGroupeTacheEntity() {}
    
    public InstanceGroupeTacheEntity(UUID id, LocalDate dateEcheance, String code, String libelle,
                                     StatutTache statut, UUID templateId, UUID processusId, List<UUID> agentAssigneIdList) {
        this.id = id;
        this.dateEcheance = dateEcheance;
        this.code = code;
        this.libelle = libelle;
        this.statut = statut;
        this.templateId = templateId;
        this.processusId = processusId;
        this.agentAssigneIdList = agentAssigneIdList != null ? new ArrayList<>(agentAssigneIdList) : new ArrayList<>();
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public LocalDate getDateEcheance() {
        return dateEcheance;
    }
    
    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
    
    public StatutTache getStatut() {
        return statut;
    }
    
    public void setStatut(StatutTache statut) {
        this.statut = statut;
    }
    
    public UUID getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }
    
    public UUID getProcessusId() {
        return processusId;
    }
    
    public void setProcessusId(UUID processusId) {
        this.processusId = processusId;
    }
    
    public List<UUID> getAgentAssigneIdList() {
        return agentAssigneIdList;
    }
    
    public void setAgentAssigneIdList(List<UUID> agentAssigneIdList) {
        this.agentAssigneIdList = agentAssigneIdList != null ? new ArrayList<>(agentAssigneIdList) : new ArrayList<>();
    }
}