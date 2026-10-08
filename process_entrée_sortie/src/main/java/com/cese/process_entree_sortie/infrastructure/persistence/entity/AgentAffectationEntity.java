package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "agent_affectation")
public class AgentAffectationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "fonction", nullable = false)
    private String fonction;
    
    @Column(name = "agent_responsable_id", columnDefinition = "UUID")
    private UUID agentResponsableId;
    
    @Column(name = "agent_acceuil_id", columnDefinition = "UUID")
    private UUID agentAcceuilId;
    
    @Column(name = "direction_id", nullable = false, columnDefinition = "UUID")
    private UUID directionId;
    
    @Column(name = "agent_id", nullable = false, columnDefinition = "UUID")
    private UUID agentId;
    
    public AgentAffectationEntity() {}
    
    public AgentAffectationEntity(String fonction, UUID agentResponsableId, 
                                  UUID agentAcceuilId, UUID directionId, UUID agentId) {
        this.fonction = fonction;
        this.agentResponsableId = agentResponsableId;
        this.agentAcceuilId = agentAcceuilId;
        this.directionId = directionId;
        this.agentId = agentId;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getFonction() {
        return fonction;
    }
    
    public void setFonction(String fonction) {
        this.fonction = fonction;
    }
    
    public UUID getAgentResponsableId() {
        return agentResponsableId;
    }
    
    public void setAgentResponsableId(UUID agentResponsableId) {
        this.agentResponsableId = agentResponsableId;
    }
    
    public UUID getAgentAcceuilId() {
        return agentAcceuilId;
    }
    
    public void setAgentAcceuilId(UUID agentAcceuilId) {
        this.agentAcceuilId = agentAcceuilId;
    }
    
    public UUID getDirectionId() {
        return directionId;
    }
    
    public void setDirectionId(UUID directionId) {
        this.directionId = directionId;
    }
    
    public UUID getAgentId() {
        return agentId;
    }
    
    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }
}