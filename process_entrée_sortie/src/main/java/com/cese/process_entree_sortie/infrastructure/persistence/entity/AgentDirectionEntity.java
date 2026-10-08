package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "agent_direction")
public class AgentDirectionEntity {
    
    @Id
    @Column(name = "agent_direction_id", columnDefinition = "UUID")
    private UUID agentDirectionID;
    
    @Column(name = "code_agent_direction")
    private String codeAgentDirection;
    
    @Column(name = "code_agent")
    private String codeAgent;
    
    @Column(name = "code_direction")
    private String codeDirection;
    
    @Column(name = "direction_id", nullable = false, columnDefinition = "UUID")
    private UUID directionId;
    
    @Column(name = "date_arrivee", nullable = false)
    private LocalDate dateArrivee;
    
    @Column(name = "date_depart")
    private LocalDate dateDepart;
    
    @Column(name = "agent_personnel_id", columnDefinition = "UUID")
    private UUID agentPersonnelId;
    
    @Column(name = "numero_bureau")
    private String numeroBureau;
    
    public AgentDirectionEntity() {}
    
    public AgentDirectionEntity(UUID agentDirectionID, String codeAgentDirection, String codeAgent, 
                               String codeDirection, UUID directionId, LocalDate dateArrivee, 
                               LocalDate dateDepart, UUID agentPersonnelId, String numeroBureau) {
        this.agentDirectionID = agentDirectionID;
        this.codeAgentDirection = codeAgentDirection;
        this.codeAgent = codeAgent;
        this.codeDirection = codeDirection;
        this.directionId = directionId;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.agentPersonnelId = agentPersonnelId;
        this.numeroBureau = numeroBureau;
    }
    
    public UUID getAgentDirectionID() {
        return agentDirectionID;
    }
    
    public void setAgentDirectionID(UUID agentDirectionID) {
        this.agentDirectionID = agentDirectionID;
    }
    
    public String getCodeAgentDirection() {
        return codeAgentDirection;
    }
    
    public void setCodeAgentDirection(String codeAgentDirection) {
        this.codeAgentDirection = codeAgentDirection;
    }
    
    public String getCodeAgent() {
        return codeAgent;
    }
    
    public void setCodeAgent(String codeAgent) {
        this.codeAgent = codeAgent;
    }
    
    public String getCodeDirection() {
        return codeDirection;
    }
    
    public void setCodeDirection(String codeDirection) {
        this.codeDirection = codeDirection;
    }
    
    public UUID getDirectionId() {
        return directionId;
    }
    
    public void setDirectionId(UUID directionId) {
        this.directionId = directionId;
    }
    
    public LocalDate getDateArrivee() {
        return dateArrivee;
    }
    
    public void setDateArrivee(LocalDate dateArrivee) {
        this.dateArrivee = dateArrivee;
    }
    
    public LocalDate getDateDepart() {
        return dateDepart;
    }
    
    public void setDateDepart(LocalDate dateDepart) {
        this.dateDepart = dateDepart;
    }
    
    public UUID getAgentPersonnelId() {
        return agentPersonnelId;
    }
    
    public void setAgentPersonnelId(UUID agentPersonnelId) {
        this.agentPersonnelId = agentPersonnelId;
    }

    public String getNumeroBureau() {
        return numeroBureau;
    }
    
    public void setNumeroBureau(String numeroBureau) {
        this.numeroBureau = numeroBureau;
    }
}