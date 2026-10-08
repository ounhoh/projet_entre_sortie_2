package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "agent_personnel")
public class AgentPersonnelEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false)
    private String nom;
    
    @Column(nullable = false)
    private String prenom;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "etat_agent", nullable = false)
    private EtatAgent etatAgent;
    
    public AgentPersonnelEntity() {}
    
    public AgentPersonnelEntity(UUID id, String nom, String prenom, String email, RoleEntity role, EtatAgent etatAgent) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.etatAgent = etatAgent;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public RoleEntity getRole() {
        return role;
    }
    
    public void setRole(RoleEntity role) {
        this.role = role;
    }
    
    public EtatAgent getEtatAgent() {
        return etatAgent;
    }
    
    public void setEtatAgent(EtatAgent etatAgent) {
        this.etatAgent = etatAgent;
    }
}