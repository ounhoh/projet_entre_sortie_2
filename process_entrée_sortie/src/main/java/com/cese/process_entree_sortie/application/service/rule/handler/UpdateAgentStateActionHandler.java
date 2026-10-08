package com.cese.process_entree_sortie.application.service.rule.handler;

import com.cese.process_entree_sortie.application.dto.rule.ActionRuleDTO;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.rule.ActionHandler;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Handler pour mettre à jour l'état d'un agent.
 * 
 * Paramètres requis:
 * - agentId: ID de l'agent à mettre à jour (peut être une expression comme "${agent.id}" ou "${processus.agentId}")
 * - etatAgent: Nouvel état de l'agent (peut être une expression comme "${processus.typeProcessus}")
 */
@Component
public class UpdateAgentStateActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(UpdateAgentStateActionHandler.class);
    
    private final AgentSpi agentSpi;
    
    public UpdateAgentStateActionHandler(AgentSpi agentSpi) {
        this.agentSpi = agentSpi;
    }
    
    @Override
    public String getActionType() {
        return "UPDATE_AGENT_STATE";
    }
    
    @Override
    public Object execute(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        UUID agentId = getAgentId(params, context);
        String etatAgentStr = getStringParam(params, "etatAgent");
        
        if (agentId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de l'agent pour UPDATE_AGENT_STATE");
        }
        
        logger.info("Mise à jour de l'état de l'agent {} vers {}", agentId, etatAgentStr);
        
        AgentPersonnel agent = agentSpi.findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));
        
        EtatAgent nouvelEtat = parseEtatAgent(etatAgentStr, context);
        
        AgentPersonnel agentModifie = agent.changerEtatAgent(nouvelEtat);
        AgentPersonnel agentSauvegarde = agentSpi.save(agentModifie);
        
        logger.info("État de l'agent {} mis à jour vers {}", agentId, nouvelEtat);
        
        // Mettre à jour dans le contexte
        context.setVariable("agent", agentSauvegarde);
        context.setVariable("agentId", agentSauvegarde.id());
        
        return agentSauvegarde.id();
    }
    
    @Override
    public boolean validate(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        // Vérifier qu'on peut déterminer l'ID de l'agent
        UUID agentId = getAgentId(params, context);
        if (agentId == null) {
            logger.warn("Impossible de déterminer l'ID de l'agent pour UPDATE_AGENT_STATE");
            return false;
        }
        
        // Vérifier que l'agent existe
        if (agentSpi.findById(agentId).isEmpty()) {
            logger.warn("Agent non trouvé avec l'ID: {} pour UPDATE_AGENT_STATE", agentId);
            return false;
        }
        
        // Vérifier que l'état est fourni
        if (!params.containsKey("etatAgent") || getStringParam(params, "etatAgent") == null) {
            logger.warn("Paramètre 'etatAgent' manquant pour UPDATE_AGENT_STATE");
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
    
    private String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }
    
    private EtatAgent parseEtatAgent(String etatAgentStr, RuleExecutionContext context) {
        if (etatAgentStr == null || etatAgentStr.isBlank()) {
            throw new IllegalArgumentException("L'état de l'agent ne peut pas être vide");
        }
        
        try {
            return EtatAgent.valueOf(etatAgentStr);
        } catch (IllegalArgumentException e) {
            // Si ce n'est pas un enum valide, essayer de mapper depuis le type de processus
            String typeProcessus = context.getTypeProcessus();
            return mapTypeProcessusToEtatAgent(etatAgentStr, typeProcessus);
        }
    }
    
    private EtatAgent mapTypeProcessusToEtatAgent(String etatAgentStr, String typeProcessus) {
        // Si c'est le type de processus qui est passé
        if (typeProcessus != null && etatAgentStr.equalsIgnoreCase(typeProcessus)) {
            return switch (typeProcessus.toLowerCase()) {
                case "entree" -> EtatAgent.entree;
                case "mobiliteinterne", "mobitliteinterne" -> EtatAgent.mobiliteInterne;
                case "sortie" -> EtatAgent.sortie;
                default -> EtatAgent.entree;
            };
        }
        
        // Sinon, essayer de mapper directement
        return switch (etatAgentStr.toLowerCase()) {
            case "entree" -> EtatAgent.entree;
            case "mobiliteinterne", "mobitliteinterne" -> EtatAgent.mobiliteInterne;
            case "sortie" -> EtatAgent.sortie;
            default -> {
                logger.warn("État d'agent inconnu: {}, utilisation de 'entree' par défaut", etatAgentStr);
                yield EtatAgent.entree;
            }
        };
    }
}
