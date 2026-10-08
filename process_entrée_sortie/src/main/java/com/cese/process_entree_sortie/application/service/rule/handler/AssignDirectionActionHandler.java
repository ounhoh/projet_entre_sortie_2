package com.cese.process_entree_sortie.application.service.rule.handler;

import com.cese.process_entree_sortie.application.dto.rule.ActionRuleDTO;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.rule.ActionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Handler pour assigner une direction à un agent.
 * 
 * Paramètres requis:
 * - agentId: ID de l'agent (peut être une expression comme "${agent.id}")
 * - directionId: ID de la direction (peut être une expression comme "${formulaire.direction}")
 * 
 * Note: Cette action nécessite probablement un service d'affectation qui n'existe pas encore.
 * Pour l'instant, on log simplement l'action.
 */
@Component
public class AssignDirectionActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AssignDirectionActionHandler.class);
    
    private final AgentSpi agentSpi;
    
    public AssignDirectionActionHandler(AgentSpi agentSpi) {
        this.agentSpi = agentSpi;
    }
    
    @Override
    public String getActionType() {
        return "ASSIGN_DIRECTION";
    }
    
    @Override
    public Object execute(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        UUID agentId = getAgentId(params, context);
        UUID directionId = getDirectionId(params, context);
        
        if (agentId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de l'agent pour ASSIGN_DIRECTION");
        }
        if (directionId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de la direction pour ASSIGN_DIRECTION");
        }
        
        logger.info("Assignation de la direction {} à l'agent {}", directionId, agentId);
        
        // TODO: Implémenter l'assignation de direction
        // Cela nécessite probablement un service d'affectation (AgentAffectationService)
        // Pour l'instant, on stocke juste dans le contexte
        
        context.setVariable("directionId", directionId);
        context.setVariable("agentDirectionAssigned", true);
        
        return directionId;
    }
    
    @Override
    public boolean validate(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        // Vérifier qu'on peut déterminer l'ID de l'agent
        UUID agentId = getAgentId(params, context);
        if (agentId == null) {
            logger.warn("Impossible de déterminer l'ID de l'agent pour ASSIGN_DIRECTION");
            return false;
        }
        
        // Vérifier que l'agent existe
        if (agentSpi.findById(agentId).isEmpty()) {
            logger.warn("Agent non trouvé avec l'ID: {} pour ASSIGN_DIRECTION", agentId);
            return false;
        }
        
        // Vérifier qu'on peut déterminer l'ID de la direction
        UUID directionId = getDirectionId(params, context);
        if (directionId == null) {
            logger.warn("Impossible de déterminer l'ID de la direction pour ASSIGN_DIRECTION");
            return false;
        }
        
        return true;
    }
    
    private UUID getAgentId(Map<String, Object> params, RuleExecutionContext context) {
        // Essayer depuis les paramètres
        Object agentIdObj = params.get("agentId");
        if (agentIdObj instanceof UUID uuid) {
            return uuid;
        }
        if (agentIdObj instanceof String str) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                // Ignorer
            }
        }
        
        // Essayer depuis le contexte (variable "agentId" ou "agent.id")
        Object contextAgentId = context.getVariable("agentId");
        if (contextAgentId instanceof UUID uuid) {
            return uuid;
        }
        
        // Essayer depuis le processus
        if (context.getProcessus() != null && context.getProcessus().agentId() != null) {
            return context.getProcessus().agentId();
        }
        
        return null;
    }
    
    private UUID getDirectionId(Map<String, Object> params, RuleExecutionContext context) {
        // Essayer depuis les paramètres
        Object directionIdObj = params.get("directionId");
        if (directionIdObj instanceof UUID uuid) {
            return uuid;
        }
        if (directionIdObj instanceof String str) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                // Ignorer
            }
        }
        
        // Essayer depuis le contexte
        Object contextDirectionId = context.getVariable("directionId");
        if (contextDirectionId instanceof UUID uuid) {
            return uuid;
        }
        
        // Essayer depuis le processus
        if (context.getProcessus() != null && context.getProcessus().directionConcerneeId() != null) {
            return context.getProcessus().directionConcerneeId();
        }
        
        return null;
    }
}
