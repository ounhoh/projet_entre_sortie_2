package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "instance_tache")
public class InstanceTacheEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "code", nullable = false)
    private String code;
    
    @Column(name = "libelle")
    private String libelle;
    
    @Column(name = "contenu", columnDefinition = "TEXT", length = Integer.MAX_VALUE)
    private String contenu; // JSON stocké comme String
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutTache statut;
    
    @Column(name = "date_echeance", nullable = false)
    private LocalDate dateEcheance;
    
    @Column(name = "template_id", nullable = false, columnDefinition = "UUID")
    private UUID templateId;
    
    @Column(name = "groupe_tache_id", columnDefinition = "UUID")
    private UUID groupeTacheId;
    
    @Column(name = "dependance_id", columnDefinition = "UUID")
    private UUID dependanceId;
    
    public InstanceTacheEntity() {}
    
    public InstanceTacheEntity(UUID id, String code, String libelle, String contenu,
                               StatutTache statut, LocalDate dateEcheance, UUID templateId,
                               UUID groupeTacheId, UUID dependanceId) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.contenu = contenu;
        this.statut = statut;
        this.dateEcheance = dateEcheance;
        this.templateId = templateId;
        this.groupeTacheId = groupeTacheId;
        this.dependanceId = dependanceId;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
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
    
    public String getContenu() {
        return contenu;
    }
    
    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
    
    public StatutTache getStatut() {
        return statut;
    }
    
    public void setStatut(StatutTache statut) {
        this.statut = statut;
    }
    
    public LocalDate getDateEcheance() {
        return dateEcheance;
    }
    
    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }
    
    public UUID getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }
    
    public UUID getGroupeTacheId() {
        return groupeTacheId;
    }
    
    public void setGroupeTacheId(UUID groupeTacheId) {
        this.groupeTacheId = groupeTacheId;
    }
    
    public UUID getDependanceId() {
        return dependanceId;
    }
    
    public void setDependanceId(UUID dependanceId) {
        this.dependanceId = dependanceId;
    }
}