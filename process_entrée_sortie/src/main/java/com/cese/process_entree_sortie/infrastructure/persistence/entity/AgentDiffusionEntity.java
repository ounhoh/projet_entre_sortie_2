package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Entité pour stocker la liste de diffusion d'un agent.
 * La liste de diffusion est stockée comme un texte avec séparateur (par défaut: virgule).
 * Format: "element1,element2,element3" ou avec un autre séparateur personnalisé.
 * 
 * Sur le front-end, on peut avoir une liste d'éléments qui sera convertie en texte séparé.
 */
@Entity
@Table(name = "agent_diffusion")
public class AgentDiffusionEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "agent_id", nullable = false, columnDefinition = "UUID")
    private UUID agentId;
    
    /**
     * Liste de diffusion stockée comme un texte avec séparateur.
     * Format par défaut: "element1,element2,element3"
     * Séparateur par défaut: virgule (",")
     * 
     * Pour convertir une List<String> en String: String.join(",", liste)
     * Pour convertir un String en List<String>: Arrays.asList(texte.split(","))
     */
    @Column(name = "liste_diffusion", columnDefinition = "TEXT", length = Integer.MAX_VALUE)
    private String listeDiffusion;
    
    /**
     * Séparateur utilisé pour diviser la liste (par défaut: ",")
     * Si null, on utilise la virgule par défaut.
     */
    @Column(name = "separateur", length = 10)
    private String separateur;
    
    public AgentDiffusionEntity() {
        this.separateur = ","; // Séparateur par défaut
    }
    
    public AgentDiffusionEntity(UUID id, UUID agentId, String listeDiffusion) {
        this.id = id;
        this.agentId = agentId;
        this.listeDiffusion = listeDiffusion != null ? listeDiffusion : "";
        this.separateur = ","; // Séparateur par défaut
    }
    
    public AgentDiffusionEntity(UUID id, UUID agentId, String listeDiffusion, String separateur) {
        this.id = id;
        this.agentId = agentId;
        this.listeDiffusion = listeDiffusion != null ? listeDiffusion : "";
        this.separateur = separateur != null ? separateur : ",";
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
    
    public String getListeDiffusion() {
        return listeDiffusion;
    }
    
    public void setListeDiffusion(String listeDiffusion) {
        this.listeDiffusion = listeDiffusion != null ? listeDiffusion : "";
    }
    
    /**
     * Convertit la liste de diffusion (String) en List<String>.
     * Utilise le séparateur stocké (ou "," par défaut).
     */
    public List<String> getListeDiffusionAsList() {
        if (listeDiffusion == null || listeDiffusion.isBlank()) {
            return new ArrayList<>();
        }
        String sep = separateur != null && !separateur.isBlank() ? separateur : ",";
        return Arrays.asList(listeDiffusion.split(sep));
    }
    
    /**
     * Définit la liste de diffusion depuis une List<String>.
     * Utilise le séparateur stocké (ou "," par défaut).
     */
    public void setListeDiffusionFromList(List<String> liste) {
        if (liste == null || liste.isEmpty()) {
            this.listeDiffusion = "";
            return;
        }
        String sep = separateur != null && !separateur.isBlank() ? separateur : ",";
        this.listeDiffusion = String.join(sep, liste);
    }
    
    public String getSeparateur() {
        return separateur != null ? separateur : ",";
    }
    
    public void setSeparateur(String separateur) {
        this.separateur = separateur != null ? separateur : ",";
    }
}
