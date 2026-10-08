package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité pour stocker les informations matérielles et droits d'un agent.
 * Les données sont stockées en JSON dans la colonne materiel_et_droit.
 */
@Entity
@Table(name = "agent_materiel_et_droit")
public class AgentMaterielEtDroitEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "agent_id", nullable = false, columnDefinition = "UUID")
    private UUID agentId;
    
    @Lob
    @Column(name = "materiel_et_droit", columnDefinition = "TEXT", length = Integer.MAX_VALUE)
    private String materielEtDroit; // JSON stocké comme String (Map<String, Object>)
    
    public AgentMaterielEtDroitEntity() {}
    
    public AgentMaterielEtDroitEntity(UUID id, UUID agentId, String materielEtDroit) {
        this.id = id;
        this.agentId = agentId;
        this.materielEtDroit = materielEtDroit;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAgentId() {
        return agentId;
    }
    
    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }
    
    public String getMaterielEtDroit() {
        return materielEtDroit;
    }
    
    public void setMaterielEtDroit(String materielEtDroit) {
        this.materielEtDroit = materielEtDroit;
    }
}
