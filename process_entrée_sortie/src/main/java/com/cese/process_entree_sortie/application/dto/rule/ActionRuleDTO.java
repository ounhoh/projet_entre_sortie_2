package com.cese.process_entree_sortie.application.dto.rule;

import java.util.Map;

/**
 * DTO représentant une règle d'action à exécuter.
 * Les règles sont définies dans le contenu JSON de la tâche.
 */
public record ActionRuleDTO(
    /**
     * Type d'action à exécuter (ex: "CREATE_AGENT", "UPDATE_AGENT_STATE", "ASSIGN_DIRECTION")
     */
    String type,
    
    /**
     * Condition optionnelle pour exécuter l'action (expression à évaluer).
     * Si null ou vide, l'action est toujours exécutée.
     * Ex: "${agent.exists}" ou "${formulaire.role} == 'Manager'"
     */
    String condition,
    
    /**
     * Paramètres de l'action avec expressions à résoudre.
     * Les valeurs peuvent contenir des expressions ${...} qui seront résolues.
     * Ex: {"nom": "${formulaire.nom_agent}", "email": "${formulaire.email_agent}"}
     */
    Map<String, Object> params
) {
    public ActionRuleDTO {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Le type d'action ne peut pas être vide");
        }
        if (params == null) {
            params = Map.of();
        }
    }
}
