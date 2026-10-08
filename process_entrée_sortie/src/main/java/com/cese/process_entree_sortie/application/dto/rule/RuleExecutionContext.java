package com.cese.process_entree_sortie.application.dto.rule;

import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contexte d'exécution des règles.
 * Contient toutes les données nécessaires pour résoudre les expressions et exécuter les actions.
 */
public class RuleExecutionContext {
    
    /**
     * Données du formulaire (valeurs soumises par l'utilisateur)
     */
    private final Map<String, Object> formulaireData;
    
    /**
     * Processus en cours
     */
    private final InstanceProcessus processus;
    
    /**
     * Tâche complétée qui déclenche l'exécution des règles
     */
    private final InstanceTache tache;
    
    /**
     * Variables additionnelles calculées ou mises en cache
     * (ex: agent créé, direction trouvée, etc.)
     */
    private final Map<String, Object> variables;
    
    public RuleExecutionContext(
            Map<String, Object> formulaireData,
            InstanceProcessus processus,
            InstanceTache tache) {
        this.formulaireData = formulaireData != null ? formulaireData : Map.of();
        this.processus = processus;
        this.tache = tache;
        this.variables = new HashMap<>();
    }
    
    /**
     * Crée un nouveau contexte avec des paramètres résolus
     */
    public RuleExecutionContext withResolvedParams(Map<String, Object> resolvedParams) {
        RuleExecutionContext newContext = new RuleExecutionContext(
            this.formulaireData,
            this.processus,
            this.tache
        );
        newContext.variables.putAll(this.variables);
        newContext.variables.putAll(resolvedParams);
        return newContext;
    }
    
    /**
     * Ajoute une variable au contexte
     */
    public void setVariable(String key, Object value) {
        this.variables.put(key, value);
    }
    
    /**
     * Récupère une variable du contexte
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }
    
    // Getters
    public Map<String, Object> getFormulaireData() {
        return formulaireData;
    }
    
    public InstanceProcessus getProcessus() {
        return processus;
    }
    
    public InstanceTache getTache() {
        return tache;
    }
    
    public Map<String, Object> getVariables() {
        return Map.copyOf(variables);
    }
    
    /**
     * Récupère une valeur du formulaire
     */
    public Object getFormulaireValue(String key) {
        return formulaireData.get(key);
    }
    
    /**
     * Récupère l'ID de l'agent du processus
     */
    public UUID getAgentId() {
        return processus != null ? processus.agentId() : null;
    }
    
    /**
     * Récupère le type de processus
     */
    public String getTypeProcessus() {
        return processus != null ? processus.typeProcessus().toString() : null;
    }
}
