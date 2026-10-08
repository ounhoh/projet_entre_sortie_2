package com.cese.process_entree_sortie.application.service.rule.handler;

import com.cese.process_entree_sortie.application.dto.rule.ActionRuleDTO;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.rule.ActionHandler;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler pour créer une AgentAffectation depuis les données du formulaire.
 * 
 * Paramètres requis:
 * - agentId: ID de l'agent (peut être une expression comme "${agent.id}")
 * - directionId: ID de la direction (peut être une expression comme "${agentDirection.directionId}")
 * - fonction: Fonction de l'agent (peut être une expression comme "${formulaire.fonction}")
 * - agentResponsableId: (optionnel) ID de l'agent responsable
 * - agentAcceuilId: (optionnel) ID de l'agent d'accueil
 */
@Component
public class CreateAgentAffectationActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateAgentAffectationActionHandler.class);
    
    private final AffectationSpi affectationSpi;
    private final AgentSpi agentSpi;
    
    public CreateAgentAffectationActionHandler(AffectationSpi affectationSpi, AgentSpi agentSpi) {
        this.affectationSpi = affectationSpi;
        this.agentSpi = agentSpi;
    }
    
    @Override
    public String getActionType() {
        return "CREATE_AGENT_AFFECTATION";
    }
    
    @Override
    public Object execute(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        UUID agentId = getUUIDParam(params, "agentId", context);
        UUID directionId = getUUIDParam(params, "directionId", context);
        String fonction = getStringParam(params, "fonction", context);
        UUID agentResponsableId = getUUIDParamOptional(params, "agentResponsableId", context);
        UUID agentAcceuilId = getUUIDParamOptional(params, "agentAcceuilId", context);
        
        if (agentId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de l'agent pour CREATE_AGENT_AFFECTATION");
        }
        if (directionId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de la direction pour CREATE_AGENT_AFFECTATION");
        }
        if (fonction == null || fonction.isBlank()) {
            throw new RuntimeException("La fonction est requise pour CREATE_AGENT_AFFECTATION");
        }
        
        logger.info("Création/mise à jour d'une AgentAffectation pour l'agent {} avec la fonction {}", agentId, fonction);
        
        // Vérifier s'il existe déjà une affectation pour cet agent et cette direction
        List<AgentAffectation> affectationsExistantes = affectationSpi.findByAgentId(agentId);
        Optional<AgentAffectation> affectationExistanteOpt = affectationsExistantes.stream()
            .filter(aff -> aff.directionId().equals(directionId))
            .findFirst();
        
        AgentAffectation saved;
        
        if (affectationExistanteOpt.isPresent()) {
            // Supprimer l'ancienne affectation et créer une nouvelle avec les données mises à jour
            AgentAffectation affectationExistante = affectationExistanteOpt.get();
            logger.info("Affectation existante trouvée pour l'agent {} et la direction {}, suppression...", agentId, directionId);
            affectationSpi.delete(affectationExistante);
            
            // Créer une nouvelle affectation avec les données mises à jour
            AgentAffectation nouvelleAffectation = new AgentAffectation(
                fonction,
                agentResponsableId != null ? agentResponsableId : affectationExistante.agentResponsableId(),
                agentAcceuilId != null ? agentAcceuilId : affectationExistante.agentAcceuilId(),
                directionId,
                agentId
            );
            
            saved = affectationSpi.save(nouvelleAffectation);
            logger.info("Affectation existante remplacée pour l'agent {}", agentId);
        } else {
            // Créer une nouvelle affectation
            AgentAffectation affectation = new AgentAffectation(
                fonction,
                agentResponsableId,
                agentAcceuilId,
                directionId,
                agentId
            );
            
            saved = affectationSpi.save(affectation);
            logger.info("Nouvelle Affectation créée pour l'agent {}", agentId);
        }
        
        // Mettre à jour l'agent pour ajouter l'affectation à sa liste
        AgentPersonnel agent = agentSpi.findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));
        
        List<AgentAffectation> nouvellesAffectations = new ArrayList<>(agent.agentAffectations());
        nouvellesAffectations.addFirst(saved);
        
        AgentPersonnel agentModifie = new AgentPersonnel(
            agent.id(),
            agent.nom(),
            agent.prenom(),
            agent.email(),
            agent.role(),
            agent.etatAgent(),
            agent.agentDirections(),
            List.copyOf(nouvellesAffectations),
            agent.agentMaterielEtDroit()
        );
        
        agentSpi.save(agentModifie);
        
        logger.info("AgentAffectation créée avec succès pour l'agent {}", agentId);
        
        // Stocker dans le contexte
        context.setVariable("agentAffectation", saved);
        
        return saved.agentId();
    }
    
    @Override
    public boolean validate(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        logger.info("=== VALIDATION CREATE_AGENT_AFFECTATION ===");
        logger.info("Paramètres reçus: {}", params);
        logger.info("formulaireData disponible: {} clés", context.getFormulaireData() != null ? context.getFormulaireData().size() : 0);
        if (context.getFormulaireData() != null && !context.getFormulaireData().isEmpty()) {
            logger.info("Clés dans formulaireData: {}", context.getFormulaireData().keySet());
        }
        
        UUID agentId = getUUIDParam(params, "agentId", context);
        if (agentId == null) {
            logger.error("Impossible de déterminer l'ID de l'agent pour CREATE_AGENT_AFFECTATION");
            return false;
        }
        
        // Vérifier que l'agent existe
        if (agentSpi.findById(agentId).isEmpty()) {
            logger.error("Agent non trouvé avec l'ID: {} pour CREATE_AGENT_AFFECTATION", agentId);
            return false;
        }
        
        UUID directionId = getUUIDParam(params, "directionId", context);
        if (directionId == null) {
            logger.error("=== ÉCHEC VALIDATION: Impossible de déterminer directionId ===");
            logger.error("Paramètre directionId brut: {}", params.get("directionId"));
            logger.error("Processus dans contexte: {}", context.getProcessus() != null ? 
                        "présent (directionConcerneeId=" + context.getProcessus().directionConcerneeId() + ")" : "NULL");
            logger.error("Variables dans contexte: agentDirection={}, agentDirectionDirectionId={}", 
                       context.getVariable("agentDirection"), context.getVariable("agentDirectionDirectionId"));
            logger.error("formulaireData: {} clés - {}", 
                       context.getFormulaireData() != null ? context.getFormulaireData().size() : 0,
                       context.getFormulaireData() != null ? context.getFormulaireData().keySet() : "null");
            return false;
        }
        
        String fonction = getStringParam(params, "fonction", context);
        logger.info("[validate] Paramètre 'fonction' brut: {}", params.get("fonction"));
        logger.info("[validate] Paramètre 'fonction' après getStringParam: {}", fonction);
        if (fonction == null || fonction.isBlank()) {
            logger.error("La fonction est requise pour CREATE_AGENT_AFFECTATION");
            logger.error("formulaireData contient 'fonction': {}", 
                       context.getFormulaireData() != null ? context.getFormulaireData().get("fonction") : "formulaireData is null");
            return false;
        }
        
        logger.info("Validation réussie: agentId={}, directionId={}, fonction={}", agentId, directionId, fonction);
        return true;
    }
    
    private UUID getUUIDParam(Map<String, Object> params, String key, RuleExecutionContext context) {
        Object value = params.get(key);
        logger.info("[getUUIDParam] Recherche de '{}': valeur brute = {} (type: {})", 
                   key, value, value != null ? value.getClass().getSimpleName() : "null");
        
        if (value instanceof UUID uuid) {
            logger.info("[getUUIDParam] '{}' trouvé comme UUID: {}", key, uuid);
            return uuid;
        }
        
        boolean valueIsEmpty = false;
        if (value instanceof String str) {
            // Si c'est une chaîne vide, considérer comme null pour activer le fallback
            if (str.isBlank()) {
                logger.info("[getUUIDParam] '{}' est une chaîne vide, activation du fallback", key);
                valueIsEmpty = true;
                value = null; // Force le fallback
            } else {
                try {
                    UUID parsed = UUID.fromString(str);
                    logger.info("[getUUIDParam] '{}' parsé depuis string: {}", key, parsed);
                    return parsed;
                } catch (IllegalArgumentException e) {
                    logger.info("[getUUIDParam] Impossible de parser '{}' comme UUID: {}", str, e.getMessage());
                }
            }
        }
        
        // Essayer depuis le contexte
        Object contextValue = context.getVariable(key);
        if (contextValue instanceof UUID uuid) {
            logger.info("[getUUIDParam] '{}' trouvé dans les variables du contexte: {}", key, uuid);
            return uuid;
        }
        if (contextValue instanceof String str && !str.isBlank()) {
            try {
                UUID parsed = UUID.fromString(str);
                logger.info("[getUUIDParam] '{}' parsé depuis variable contexte: {}", key, parsed);
                return parsed;
            } catch (IllegalArgumentException e) {
                // Ignorer
            }
        }
        
        // Essayer avec "agentId" si on cherche "agentId"
        if (key.equals("agentId")) {
            Object agentId = context.getVariable("agentId");
            if (agentId instanceof UUID uuid) {
                return uuid;
            }
            if (context.getProcessus() != null && context.getProcessus().agentId() != null) {
                UUID processusAgentId = context.getProcessus().agentId();
                logger.info("[getUUIDParam] agentId récupéré depuis processus.agentId: {}", processusAgentId);
                return processusAgentId;
            }
        }
        
        // Essayer avec "agentDirection.directionId" si on cherche "directionId"
        if (key.equals("directionId")) {
            logger.info("[getUUIDParam] === Recherche spécifique de directionId ===");
            
            // 1. D'abord essayer via agentDirection dans le contexte (si CREATE_AGENT_DIRECTION a été exécuté)
            Object agentDirection = context.getVariable("agentDirection");
            logger.info("[getUUIDParam] agentDirection dans contexte: {}", agentDirection != null ? agentDirection.getClass().getSimpleName() : "null");
            if (agentDirection instanceof com.cese.process_entree_sortie.domain.Agent.model.AgentDirection ad) {
                UUID dirId = ad.directionId();
                logger.info("[getUUIDParam] directionId trouvé via agentDirection: {}", dirId);
                return dirId;
            }
            
            // 2. Essayer via agentDirectionDirectionId (variable créée par CREATE_AGENT_DIRECTION)
            Object directionId = context.getVariable("agentDirectionDirectionId");
            logger.info("[getUUIDParam] agentDirectionDirectionId dans contexte: {}", directionId);
            if (directionId instanceof UUID uuid) {
                logger.info("[getUUIDParam] directionId trouvé via agentDirectionDirectionId: {}", uuid);
                return uuid;
            }
            
            // 3. En dernier recours, utiliser la direction concernée du processus
            logger.info("[getUUIDParam] Tentative de récupération depuis processus...");
            logger.info("[getUUIDParam] Processus dans contexte: {}", context.getProcessus() != null ? "présent" : "NULL");
            if (context.getProcessus() != null) {
                UUID processusDirectionId = context.getProcessus().directionConcerneeId();
                logger.info("[getUUIDParam] processus.directionConcerneeId = {}", processusDirectionId);
                if (processusDirectionId != null) {
                    logger.info("[getUUIDParam] SUCCÈS: directionId récupéré depuis processus.directionConcerneeId: {}", processusDirectionId);
                    return processusDirectionId;
                } else {
                    logger.warn("[getUUIDParam] processus.directionConcerneeId est NULL");
                }
            } else {
                logger.error("[getUUIDParam] Le processus est NULL dans le contexte !");
            }
            
            // 4. Fallback supplémentaire : chercher dans formulaireData si le processus n'a pas de directionConcerneeId
            Map<String, Object> formulaireData = context.getFormulaireData();
            logger.info("[getUUIDParam] Tentative de récupération depuis formulaireData...");
            logger.info("[getUUIDParam] formulaireData: {} clés, valeur null/vide: {}", 
                       formulaireData != null ? formulaireData.size() : 0, value == null || valueIsEmpty);
            if (formulaireData != null && (value == null || valueIsEmpty)) {
                logger.info("[getUUIDParam] Fallback formulaireData activé ({} clés disponibles): {}", 
                           formulaireData.size(), formulaireData.keySet());
                Object directionObj = formulaireData.get("direction");
                logger.info("[getUUIDParam] Valeur de 'direction' dans formulaireData: {} (type: {})", 
                           directionObj, directionObj != null ? directionObj.getClass().getSimpleName() : "null");
                if (directionObj instanceof UUID directionUuid) {
                    logger.info("[getUUIDParam] SUCCÈS: directionId trouvé dans formulaireData via clé 'direction': {}", directionUuid);
                    return directionUuid;
                }
                if (directionObj instanceof String directionStr && !directionStr.isBlank()) {
                    try {
                        UUID directionUuid = UUID.fromString(directionStr);
                        logger.info("[getUUIDParam] SUCCÈS: directionId trouvé dans formulaireData via clé 'direction' (string): {}", directionUuid);
                        return directionUuid;
                    } catch (IllegalArgumentException e) {
                        logger.warn("[getUUIDParam] Impossible de parser 'direction' comme UUID: {}", e.getMessage());
                    }
                }
                Object directionIdObj = formulaireData.get("directionId");
                logger.info("[getUUIDParam] Valeur de 'directionId' dans formulaireData: {} (type: {})", 
                           directionIdObj, directionIdObj != null ? directionIdObj.getClass().getSimpleName() : "null");
                if (directionIdObj instanceof UUID directionUuid) {
                    logger.info("[getUUIDParam] SUCCÈS: directionId trouvé dans formulaireData via clé 'directionId': {}", directionUuid);
                    return directionUuid;
                }
                if (directionIdObj instanceof String directionIdStr && !directionIdStr.isBlank()) {
                    try {
                        UUID directionUuid = UUID.fromString(directionIdStr);
                        logger.info("[getUUIDParam] SUCCÈS: directionId trouvé dans formulaireData via clé 'directionId' (string): {}", directionUuid);
                        return directionUuid;
                    } catch (IllegalArgumentException e) {
                        logger.warn("[getUUIDParam] Impossible de parser 'directionId' comme UUID: {}", e.getMessage());
                    }
                }
                logger.error("[getUUIDParam] ÉCHEC: directionId non trouvé dans formulaireData. Clés disponibles: {}", formulaireData.keySet());
            } else {
                if (formulaireData == null) {
                    logger.warn("[getUUIDParam] formulaireData est NULL, impossible d'utiliser le fallback");
                } else {
                    logger.warn("[getUUIDParam] Fallback formulaireData non activé car value={}, valueIsEmpty={}", value, valueIsEmpty);
                }
            }
            
            logger.error("[getUUIDParam] ÉCHEC COMPLET: Aucun directionId trouvé pour '{}'", key);
        }
        
        return null;
    }
    
    private UUID getUUIDParamOptional(Map<String, Object> params, String key, RuleExecutionContext context) {
        if (!params.containsKey(key)) {
            return null;
        }
        return getUUIDParam(params, key, context);
    }
    
    private String getStringParam(Map<String, Object> params, String key, RuleExecutionContext context) {
        Object value = params.get(key);
        logger.info("[getStringParam] Recherche de '{}': valeur brute = {} (type: {})", 
                   key, value, value != null ? value.getClass().getSimpleName() : "null");
        
        boolean valueIsEmpty = false;
        if (value == null) {
            logger.warn("[getStringParam] '{}' est null, activation du fallback", key);
        } else if (value instanceof String str && str.isBlank()) {
            logger.warn("[getStringParam] '{}' est une chaîne vide, activation du fallback", key);
            valueIsEmpty = true;
        }
        
        // Si la valeur est null ou vide, chercher dans formulaireData
        if (value == null || valueIsEmpty) {
            Map<String, Object> formulaireData = context.getFormulaireData();
            if (formulaireData != null) {
                logger.info("[getStringParam] Fallback activé: recherche de '{}' dans formulaireData ({} clés disponibles): {}", 
                           key, formulaireData.size(), formulaireData.keySet());
                Object fonctionObj = formulaireData.get(key);
                if (fonctionObj != null && !fonctionObj.toString().isBlank()) {
                    String fonctionValue = fonctionObj.toString();
                    logger.info("[getStringParam] SUCCÈS: '{}' trouvé dans formulaireData: {}", key, fonctionValue);
                    return fonctionValue;
                }
                logger.warn("[getStringParam] '{}' non trouvé dans formulaireData ou valeur vide. Clés disponibles: {}", key, formulaireData.keySet());
            } else {
                logger.warn("[getStringParam] formulaireData est NULL, impossible d'utiliser le fallback");
            }
        }
        
        return value != null && !(value instanceof String && ((String) value).isBlank()) ? value.toString() : null;
    }
}
