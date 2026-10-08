package com.cese.process_entree_sortie.application.service.rule.handler;

import com.cese.process_entree_sortie.application.dto.rule.ActionRuleDTO;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.rule.ActionHandler;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler pour créer ou mettre à jour une AgentAffectation depuis les données du formulaire (UPSERT).
 * Utilise la même logique que CreateAgentAffectationActionHandler mais pour le type UPDATE_AGENT_AFFECTATION.
 * 
 * Paramètres requis:
 * - agentId: ID de l'agent (peut être une expression comme "${agent.id}")
 * - directionId: ID de la direction (peut être une expression comme "${agentDirection.directionId}")
 * - fonction: Fonction de l'agent (peut être une expression comme "${formulaire.fonction}")
 * - agentResponsableId: (optionnel) ID de l'agent responsable
 * - agentAcceuilId: (optionnel) ID de l'agent d'accueil
 */
@Component
public class UpdateAgentAffectationActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(UpdateAgentAffectationActionHandler.class);
    
    private final AffectationSpi affectationSpi;
    private final AgentSpi agentSpi;
    
    public UpdateAgentAffectationActionHandler(AffectationSpi affectationSpi, AgentSpi agentSpi) {
        this.affectationSpi = affectationSpi;
        this.agentSpi = agentSpi;
    }
    
    @Override
    public String getActionType() {
        return "UPDATE_AGENT_AFFECTATION";
    }
    
    @Override
    public Object execute(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        logger.info("UPDATE_AGENT_AFFECTATION params reçus: {}", params);

        Object rawResponsable = params.get("agentResponsableId");
        Object rawAcceuil = params.get("agentAcceuilId");
        logger.info("UPDATE_AGENT_AFFECTATION raw agentResponsableId: {} (type: {})",
            rawResponsable, rawResponsable != null ? rawResponsable.getClass().getSimpleName() : "null");
        logger.info("UPDATE_AGENT_AFFECTATION raw agentAcceuilId: {} (type: {})",
            rawAcceuil, rawAcceuil != null ? rawAcceuil.getClass().getSimpleName() : "null");
        if (rawResponsable instanceof String str && !str.isBlank()) {
            try {
                UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                logger.warn("agentResponsableId n'est pas un UUID valide: {}", str);
            }
        }
        if (rawAcceuil instanceof String str && !str.isBlank()) {
            try {
                UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                logger.warn("agentAcceuilId n'est pas un UUID valide: {}", str);
            }
        }
        if (context.getFormulaireData() != null) {
            Object formResponsable = context.getFormulaireData().get("Responsable");
            Object formAcceuil = context.getFormulaireData().get("agent_acceuil");
            logger.info("Formulaire Responsable: {} (type: {})",
                formResponsable, formResponsable != null ? formResponsable.getClass().getSimpleName() : "null");
            logger.info("Formulaire agent_acceuil: {} (type: {})",
                formAcceuil, formAcceuil != null ? formAcceuil.getClass().getSimpleName() : "null");
        }
        
        UUID agentId = getUUIDParam(params, "agentId", context);
        UUID directionId = getUUIDParam(params, "directionId", context);
        String fonction = getStringParam(params, "fonction", context);
        UUID agentResponsableId = getUUIDParamOptional(params, "agentResponsableId", context);
        UUID agentAcceuilId = getUUIDParamOptional(params, "agentAcceuilId", context);
        
        logger.info(
            "UPDATE_AGENT_AFFECTATION params résolus: agentId={}, directionId={}, fonction={}, agentResponsableId={}, agentAcceuilId={}",
            agentId, directionId, fonction, agentResponsableId, agentAcceuilId
        );
        
        if (agentId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de l'agent pour UPDATE_AGENT_AFFECTATION");
        }
        if (directionId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de la direction pour UPDATE_AGENT_AFFECTATION");
        }
        if (fonction == null || fonction.isBlank()) {
            throw new RuntimeException("La fonction est requise pour UPDATE_AGENT_AFFECTATION");
        }
        
        logger.info("Création/mise à jour d'une AgentAffectation (UPDATE_AGENT_AFFECTATION) pour l'agent {} avec la fonction {}", agentId, fonction);
        
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
        
        logger.info("AgentAffectation créée avec succès pour l'agent {}", agentId);
        
        // Stocker dans le contexte
        context.setVariable("agentAffectation", saved);
        
        return saved.agentId();
    }
    
    @Override
    public boolean validate(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        UUID agentId = getUUIDParam(params, "agentId", context);
        if (agentId == null) {
            logger.warn("Impossible de déterminer l'ID de l'agent pour UPDATE_AGENT_AFFECTATION");
            return false;
        }
        
        // Vérifier que l'agent existe
        if (agentSpi.findById(agentId).isEmpty()) {
            logger.warn("Agent non trouvé avec l'ID: {} pour UPDATE_AGENT_AFFECTATION", agentId);
            return false;
        }
        
        UUID directionId = getUUIDParam(params, "directionId", context);
        if (directionId == null) {
            logger.warn("Impossible de déterminer l'ID de la direction pour UPDATE_AGENT_AFFECTATION");
            return false;
        }
        
        String fonction = getStringParam(params, "fonction", context);
        if (fonction == null || fonction.isBlank()) {
            logger.warn("La fonction est requise pour UPDATE_AGENT_AFFECTATION");
            return false;
        }
        
        return true;
    }
    
    private UUID getUUIDParam(Map<String, Object> params, String key, RuleExecutionContext context) {
        Object value = params.get(key);
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                // Ignorer
            }
        }
        
        // Essayer depuis le contexte
        Object contextValue = context.getVariable(key);
        if (contextValue instanceof UUID uuid) {
            return uuid;
        }
        
        // Essayer avec "agentId" si on cherche "agentId"
        if (key.equals("agentId")) {
            Object agentId = context.getVariable("agentId");
            if (agentId instanceof UUID uuid) {
                return uuid;
            }
            if (context.getProcessus() != null && context.getProcessus().agentId() != null) {
                return context.getProcessus().agentId();
            }
        }
        
        // Essayer avec "agentDirection.directionId" si on cherche "directionId"
        if (key.equals("directionId")) {
            Object agentDirection = context.getVariable("agentDirection");
            if (agentDirection instanceof com.cese.process_entree_sortie.domain.Agent.model.AgentDirection ad) {
                return ad.directionId();
            }
            Object directionId = context.getVariable("agentDirectionDirectionId");
            if (directionId instanceof UUID uuid) {
                return uuid;
            }
            if (context.getProcessus() != null && context.getProcessus().directionConcerneeId() != null) {
                return context.getProcessus().directionConcerneeId();
            }
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
        
        if (value == null || (value instanceof String str && str.isBlank())) {
            Map<String, Object> formulaireData = context.getFormulaireData();
            if (formulaireData != null) {
                Object fonctionObj = formulaireData.get(key);
                if (fonctionObj != null && !fonctionObj.toString().isBlank()) {
                    return fonctionObj.toString();
                }
            }
        }
        
        return value != null && !(value instanceof String && ((String) value).isBlank()) ? value.toString() : null;
    }
}
