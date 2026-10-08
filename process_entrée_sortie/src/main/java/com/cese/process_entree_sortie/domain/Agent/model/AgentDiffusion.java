package com.cese.process_entree_sortie.domain.Agent.model;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Modèle de domaine pour la liste de diffusion d'un agent.
 * 
 * La liste de diffusion est stockée comme un texte avec séparateur (par défaut: virgule).
 * Format: "element1,element2,element3"
 * 
 * Sur le front-end, on peut avoir une liste d'éléments qui sera convertie en texte séparé.
 */
public record AgentDiffusion(String listeDiffusion, String separateur, UUID agentId) {
    
    public AgentDiffusion {
        if (listeDiffusion == null) {
            listeDiffusion = "";
        }
        if (separateur == null || separateur.isBlank()) {
            separateur = ",";
        }
    }
    
    /**
     * Constructeur avec séparateur par défaut (virgule).
     */
    public AgentDiffusion(String listeDiffusion, UUID agentId) {
        this(listeDiffusion, ",", agentId);
    }
    
    /**
     * Convertit la liste de diffusion (String) en List<String>.
     */
    public List<String> getListeDiffusionAsList() {
        if (listeDiffusion == null || listeDiffusion.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.asList(listeDiffusion.split(separateur));
    }
    
    /**
     * Crée un AgentDiffusion depuis une List<String>.
     */
    public static AgentDiffusion fromList(List<String> liste, UUID agentId) {
        return fromList(liste, ",", agentId);
    }
    
    /**
     * Crée un AgentDiffusion depuis une List<String> avec un séparateur personnalisé.
     */
    public static AgentDiffusion fromList(List<String> liste, String separateur, UUID agentId) {
        if (liste == null || liste.isEmpty()) {
            return new AgentDiffusion("", separateur, agentId);
        }
        String sep = separateur != null && !separateur.isBlank() ? separateur : ",";
        String texte = String.join(sep, liste);
        return new AgentDiffusion(texte, sep, agentId);
    }
}
