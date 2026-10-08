package com.cese.process_entree_sortie.application.service.rule;

import com.cese.process_entree_sortie.application.dto.rule.ActionRuleDTO;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.cese.process_entree_sortie.application.port.out.rule.ActionHandler;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.Tache.model.TacheContenu;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Moteur de règles pour exécuter les actions définies dans le contenu des tâches.
 * 
 * Les règles sont définies dans le contenu JSON de la tâche sous la clé "actions".
 * Exemple:
 * {
 *   "fields": [...],
 *   "actions": [
 *     {
 *       "type": "CREATE_AGENT",
 *       "condition": null,
 *       "params": {
 *         "nom": "${formulaire.nom_agent}",
 *         "email": "${formulaire.email_agent}"
 *       }
 *     }
 *   ]
 * }
 */
@Service
@Transactional
public class RuleEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleEngine.class);
    
    private final Map<String, ActionHandler> actionHandlers;
    private final ExpressionEvaluator expressionEvaluator;
    private final ObjectMapper objectMapper;
    
    public RuleEngine(List<ActionHandler> handlers, ExpressionEvaluator expressionEvaluator, ObjectMapper objectMapper) {
        this.expressionEvaluator = expressionEvaluator;
        this.objectMapper = objectMapper;
        this.actionHandlers = handlers.stream()
            .collect(Collectors.toMap(ActionHandler::getActionType, h -> h, (h1, h2) -> h1));
        
        logger.info("RuleEngine initialisé avec {} action handlers: {}", 
                   actionHandlers.size(), actionHandlers.keySet());
    }
    
    /**
     * Exécute toutes les règles définies dans le contenu de la tâche.
     * Les actions sont exécutées une par une, et en cas d'erreur, toutes les actions précédentes sont annulées (rollback).
     * 
     * @param tache La tâche complétée contenant les règles à exécuter
     * @param context Le contexte d'exécution avec les données nécessaires
     */
    public void executeRules(InstanceTache tache, RuleExecutionContext context) {
        List<ActionRuleDTO> rules = extractRulesFromTache(tache);
        
        if (rules.isEmpty()) {
            logger.debug("Aucune règle trouvée dans la tâche {}", tache.id());
            return;
        }
        
        logger.info("Exécution de {} règles pour la tâche {}", rules.size(), tache.id());
        
        // Liste pour stocker les résultats des actions exécutées (pour rollback)
        List<RollbackInfo> executedActions = new ArrayList<>();
        
        try {
            // Exécuter les actions une par une
            for (ActionRuleDTO rule : rules) {
                try {
                    // Exécuter l'action
                    Object result = executeRuleWithRollback(rule, context);
                    
                    // Stocker l'information pour le rollback si nécessaire
                    ActionHandler handler = actionHandlers.get(rule.type());
                    if (handler != null && handler.supportsRollback()) {
                        executedActions.add(new RollbackInfo(rule, handler, result, context));
                    }
                    
                    logger.info("Action {} exécutée avec succès", rule.type());
                    
                } catch (Exception e) {
                    logger.error("Erreur lors de l'exécution de l'action {}: {}", rule.type(), e.getMessage(), e);
                    
                    // Rollback : annuler toutes les actions précédentes
                    logger.info("Démarrage du rollback pour {} actions", executedActions.size());
                    rollbackActions(executedActions);
                    
                    throw new RuntimeException("Erreur lors de l'exécution de l'action " + rule.type() + ": " + e.getMessage(), e);
                }
            }
            
            logger.info("Toutes les règles ont été exécutées avec succès pour la tâche {}", tache.id());
            
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'exécution des règles pour la tâche {}: {}", 
                       tache.id(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Exécute une règle et retourne son résultat pour le rollback si nécessaire
     */
    private Object executeRuleWithRollback(ActionRuleDTO rule, RuleExecutionContext context) {
        // Évaluer la condition si présente
        if (rule.condition() != null && !rule.condition().isBlank()) {
            Object conditionResult = expressionEvaluator.evaluate(rule.condition(), context);
            boolean shouldExecute = Boolean.TRUE.equals(conditionResult) || 
                                  "true".equalsIgnoreCase(String.valueOf(conditionResult));
            
            if (!shouldExecute) {
                logger.debug("Condition non remplie pour la règle {}, skip", rule.type());
                return null;
            }
        }
        
        // Résoudre les paramètres (remplacer les expressions ${...})
        Map<String, Object> resolvedParams = resolveParams(rule.params(), context);
        
        // Créer une nouvelle règle avec les paramètres résolus
        ActionRuleDTO resolvedRule = new ActionRuleDTO(
            rule.type(),
            rule.condition(),
            resolvedParams
        );
        
        // Trouver le handler approprié
        ActionHandler handler = actionHandlers.get(rule.type());
        if (handler == null) {
            logger.warn("Aucun handler trouvé pour le type d'action: {}", rule.type());
            return null;
        }
        
        // Valider la règle
        if (!handler.validate(resolvedRule, context)) {
            logger.warn("Validation échouée pour la règle de type: {}", rule.type());
            throw new RuntimeException("Validation échouée pour la règle de type: " + rule.type());
        }
        
        // Exécuter l'action
        logger.info("Exécution de l'action: {}", rule.type());
        Object result = handler.execute(resolvedRule, context);
        
        // Stocker le résultat dans le contexte si nécessaire
        if (result != null) {
            // Par convention, si le résultat est un UUID, on le stocke avec le nom du type d'action
            if (result instanceof java.util.UUID uuid) {
                String variableName = rule.type().toLowerCase().replace("_", "");
                context.setVariable(variableName + "Id", uuid);
                context.setVariable(variableName, result);
            }
        }
        
        return result;
    }
    
    /**
     * Annule toutes les actions exécutées (rollback)
     */
    private void rollbackActions(List<RollbackInfo> executedActions) {
        // Parcourir les actions en ordre inverse pour le rollback
        for (int i = executedActions.size() - 1; i >= 0; i--) {
            RollbackInfo rollbackInfo = executedActions.get(i);
            try {
                logger.info("Rollback de l'action: {}", rollbackInfo.rule().type());
                rollbackInfo.handler().rollback(rollbackInfo.rule(), rollbackInfo.result(), rollbackInfo.context());
                logger.info("Rollback de l'action {} réussi", rollbackInfo.rule().type());
            } catch (Exception e) {
                logger.error("Erreur lors du rollback de l'action {}: {}", 
                           rollbackInfo.rule().type(), e.getMessage(), e);
                // Continuer le rollback même si une action échoue
            }
        }
    }
    
    /**
     * Classe interne pour stocker les informations nécessaires au rollback
     */
    private record RollbackInfo(
        ActionRuleDTO rule,
        ActionHandler handler,
        Object result,
        RuleExecutionContext context
    ) {}
    
    
    /**
     * Extrait les règles du contenu JSON de la tâche
     */
    private List<ActionRuleDTO> extractRulesFromTache(InstanceTache tache) {
        TacheContenu contenu = tache.contenu();
        if (contenu == null || contenu.contenuTache() == null || contenu.contenuTache().isEmpty()) {
            logger.warn("Aucune action trouvée (contenu vide) pour la tâche {} ({})", tache.code(), tache.id());
            return List.of();
        }
        
        try {
            Object actionsObj = contenu.contenuTache().get("actions");
            if (actionsObj == null) {
                logger.warn("Aucune action trouvée (clé actions absente) pour la tâche {} ({})", tache.code(), tache.id());
                return List.of();
            }
            
            // Si c'est déjà une liste
            if (actionsObj instanceof List<?> actionsList) {
                List<ActionRuleDTO> rules = new ArrayList<>();
                for (Object actionObj : actionsList) {
                    ActionRuleDTO rule = convertToActionRule(actionObj);
                    if (rule != null) {
                        rules.add(rule);
                    }
                }
                if (rules.isEmpty()) {
                    logger.warn("Actions vides après parsing pour la tâche {} ({})", tache.code(), tache.id());
                } else {
                    logger.info("Actions extraites pour la tâche {} ({}): {}", tache.code(), tache.id(),
                        rules.stream().map(ActionRuleDTO::type).toList());
                }
                return rules;
            }
            
            // Si c'est un JSON string, le parser
            if (actionsObj instanceof String jsonString) {
                List<Map<String, Object>> actionsList = objectMapper.readValue(
                    jsonString, 
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                return actionsList.stream()
                    .map(this::convertMapToActionRule)
                    .filter(rule -> rule != null)
                    .collect(Collectors.toList());
            }
            
            return List.of();
            
        } catch (Exception e) {
            logger.warn("Erreur lors de l'extraction des règles de la tâche {}: {}", 
                       tache.id(), e.getMessage());
            return List.of();
        }
    }
    
    private ActionRuleDTO convertToActionRule(Object actionObj) {
        if (actionObj instanceof Map<?, ?> map) {
            return convertMapToActionRule((Map<String, Object>) map);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private ActionRuleDTO convertMapToActionRule(Map<String, Object> map) {
        try {
            String type = (String) map.get("type");
            String condition = map.containsKey("condition") ? (String) map.get("condition") : null;
            Map<String, Object> params = map.containsKey("params") ? 
                (Map<String, Object>) map.get("params") : Map.of();
            
            if (type == null || type.isBlank()) {
                logger.warn("Règle sans type, ignorée");
                return null;
            }
            
            return new ActionRuleDTO(type, condition, params);
        } catch (Exception e) {
            logger.warn("Erreur lors de la conversion d'une règle: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Résout tous les paramètres en remplaçant les expressions ${...}
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveParams(Map<String, Object> params, RuleExecutionContext context) {
        if (params == null || params.isEmpty()) {
            return Map.of();
        }
        
        Map<String, Object> resolved = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object resolvedValue = expressionEvaluator.resolve(entry.getValue(), context);
            resolved.put(entry.getKey(), resolvedValue);
        }
        
        return resolved;
    }
}
