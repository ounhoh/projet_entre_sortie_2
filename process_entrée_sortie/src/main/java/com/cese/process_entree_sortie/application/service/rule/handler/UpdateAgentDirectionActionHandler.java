package com.cese.process_entree_sortie.application.service.rule.handler;

import com.cese.process_entree_sortie.application.dto.rule.ActionRuleDTO;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.cese.process_entree_sortie.application.port.out.agent.AgentDirectionSpi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.application.port.out.rule.ActionHandler;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDirection;
import com.cese.process_entree_sortie.domain.Direction.model.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler pour créer ou mettre à jour une AgentDirection depuis les données du formulaire (UPSERT).
 * Utilise la même logique que CreateAgentDirectionActionHandler mais pour le type UPDATE_AGENT_DIRECTION.
 * 
 * Paramètres requis:
 * - agentId: ID de l'agent (peut être une expression comme "${agent.id}")
 * - directionId: ID de la direction (peut être une expression comme "${formulaire.direction}")
 * - dateArrivee: Date d'arrivée (peut être une expression comme "${formulaire.date_arrivee}")
 */
@Component
public class UpdateAgentDirectionActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(UpdateAgentDirectionActionHandler.class);
    
    private final AgentDirectionSpi agentDirectionSpi;
    private final DirectionSpi directionSpi;
    
    public UpdateAgentDirectionActionHandler(AgentDirectionSpi agentDirectionSpi, DirectionSpi directionSpi) {
        this.agentDirectionSpi = agentDirectionSpi;
        this.directionSpi = directionSpi;
    }
    
    @Override
    public String getActionType() {
        return "UPDATE_AGENT_DIRECTION";
    }
    
    @Override
    public Object execute(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        UUID agentId = getUUIDParam(params, "agentId", context);
        UUID directionId = getUUIDParam(params, "directionId", context);
        LocalDate dateArrivee = getDateParam(params, "dateArrivee", context);
        LocalDate dateDepart = getDateParam(params, "dateDepart", context);
        String numeroBureau = getStringParam(params, "numeroBureau", context);
        if (agentId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de l'agent pour UPDATE_AGENT_DIRECTION");
        }
        if (directionId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de la direction pour UPDATE_AGENT_DIRECTION");
        }
        if (dateArrivee == null) {
            dateArrivee = LocalDate.now(); // Par défaut, utiliser la date actuelle
        }
        
        logger.info("Création/mise à jour d'une AgentDirection (UPDATE_AGENT_DIRECTION) pour l'agent {} et la direction {}", agentId, directionId);
        
        // Récupérer la direction pour avoir le code
        Direction direction = directionSpi.findById(directionId)
            .orElseThrow(() -> new RuntimeException("Direction non trouvée avec l'ID: " + directionId));
        
        // Vérifier s'il existe déjà une AgentDirection active pour cet agent et cette direction
        List<AgentDirection> directionsActives = agentDirectionSpi.fingActivesByAgentId(agentId);
        Optional<AgentDirection> existingDirectionOpt = directionsActives.stream()
            .filter(ad -> ad.directionId().equals(directionId))
            .findFirst();
        
        AgentDirection saved;
        
        if (existingDirectionOpt.isPresent()) {
            // Mettre à jour l'AgentDirection existante
            AgentDirection existingDirection = existingDirectionOpt.get();
            logger.info("AgentDirection existante trouvée avec l'ID: {}, mise à jour...", existingDirection.agentDirectionID());
            
            AgentDirection agentDirectionUpdatee = new AgentDirection(
                existingDirection.agentDirectionID(),
                existingDirection.codeAgentDirection(), // Conserver le code
                existingDirection.codeAgent(), // Conserver le codeAgent
                existingDirection.codeDirection(), // Conserver le codeDirection
                directionId,
                dateArrivee, // Mettre à jour la date d'arrivée
                dateDepart, // Mettre à jour la date de départ
                agentId,
                numeroBureau
            );
            
            saved = agentDirectionSpi.save(agentDirectionUpdatee);
            logger.info("AgentDirection existante mise à jour avec l'ID: {}", saved.agentDirectionID());
        } else {
            // Créer une nouvelle AgentDirection
            // Générer un code unique pour l'agent direction
            String codeAgentDirection = "AD_" + agentId.toString().substring(0, 8) + "_" + direction.codeDirection();
            
            AgentDirection agentDirection = new AgentDirection(
                UUID.randomUUID(),
                codeAgentDirection,
                agentId.toString(), // codeAgent
                direction.codeDirection(), // codeDirection
                directionId,
                dateArrivee,
                dateDepart, // dateDepart
                agentId,
                numeroBureau
            );
            
            saved = agentDirectionSpi.save(agentDirection);
            logger.info("Nouvelle AgentDirection créée avec l'ID: {}", saved.agentDirectionID());
        }
        
        // Stocker dans le contexte pour les actions suivantes
        context.setVariable("agentDirection", saved);
        context.setVariable("agentDirectionId", saved.agentDirectionID());
        context.setVariable("agentDirectionDirectionId", saved.directionId());
        
        return saved.agentDirectionID();
    }
    
    @Override
    public boolean validate(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        UUID agentId = getUUIDParam(params, "agentId", context);
        if (agentId == null) {
            logger.warn("Impossible de déterminer l'ID de l'agent pour UPDATE_AGENT_DIRECTION");
            return false;
        }
        
        UUID directionId = getUUIDParam(params, "directionId", context);
        if (directionId == null) {
            logger.warn("Impossible de déterminer l'ID de la direction pour UPDATE_AGENT_DIRECTION");
            return false;
        }
        
        // Vérifier que la direction existe
        if (directionSpi.findById(directionId).isEmpty()) {
            logger.warn("Direction non trouvée avec l'ID: {} pour UPDATE_AGENT_DIRECTION", directionId);
            return false;
        }
        
        return true;
    }
    
    private UUID getUUIDParam(Map<String, Object> params, String key, RuleExecutionContext context) {
        Object value = params.get(key);
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof String str) {
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
        if (contextValue instanceof String str) {
            try {
                return UUID.fromString(str);
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
                return context.getProcessus().agentId();
            }
        }
        
        return null;
    }
    
    private LocalDate getDateParam(Map<String, Object> params, String key, RuleExecutionContext context) {
        Object value = params.get(key);
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof String str) {
            try {
                return LocalDate.parse(str);
            } catch (Exception e) {
                logger.warn("Impossible de parser la date '{}': {}", str, e.getMessage());
            }
        }
        return null;
    }
    
    private String getStringParam(Map<String, Object> params, String key, RuleExecutionContext context) {
        Object value = params.get(key);
        if (value instanceof String str) {
            return str;
        }
        return null;
    }
}
