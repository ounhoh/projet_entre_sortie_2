package com.cese.process_entree_sortie.application.port.out.rule;

import com.cese.process_entree_sortie.application.dto.rule.ActionRuleDTO;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;

/**
 * Interface pour les handlers d'actions.
 * Chaque handler est responsable d'exécuter un type d'action spécifique.
 */
public interface ActionHandler {
    
    /**
     * Retourne le type d'action que ce handler peut traiter.
     * Ex: "CREATE_AGENT", "UPDATE_AGENT_STATE", "ASSIGN_DIRECTION"
     */
    String getActionType();
    
    /**
     * Exécute l'action avec les paramètres fournis.
     * 
     * @param rule La règle d'action à exécuter
     * @param context Le contexte d'exécution avec toutes les données nécessaires
     * @return Le résultat de l'exécution (peut être un UUID, un objet, etc.)
     */
    Object execute(ActionRuleDTO rule, RuleExecutionContext context);
    
    /**
     * Valide que les paramètres requis sont présents et valides.
     * 
     * @param rule La règle d'action à valider
     * @param context Le contexte d'exécution
     * @return true si les paramètres sont valides, false sinon
     */
    boolean validate(ActionRuleDTO rule, RuleExecutionContext context);
    
    /**
     * Indique si ce handler supporte le rollback.
     * 
     * @return true si le rollback est supporté, false sinon
     */
    default boolean supportsRollback() {
        return false;
    }
    
    /**
     * Annule l'action précédemment exécutée (rollback).
     * Cette méthode est appelée uniquement si supportsRollback() retourne true.
     * 
     * @param rule La règle d'action qui a été exécutée
     * @param result Le résultat de l'exécution précédente
     * @param context Le contexte d'exécution
     */
    default void rollback(ActionRuleDTO rule, Object result, RuleExecutionContext context) {
        // Implémentation par défaut : ne fait rien
        // Les handlers qui supportent le rollback doivent surcharger cette méthode
    }
}
