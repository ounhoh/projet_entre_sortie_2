package com.cese.process_entree_sortie.application.service.rule.handler;

import com.cese.process_entree_sortie.application.dto.rule.ActionRuleDTO;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.cese.process_entree_sortie.application.port.out.agent.AgentDiffusionSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.rule.ActionHandler;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDiffusion;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handler pour créer ou mettre à jour une AgentDiffusion depuis les données du formulaire (UPSERT).
 * Utilise la même logique que CreateAgentDiffusionActionHandler mais pour le type UPDATE_AGENT_DIFFUSION.
 * 
 * Paramètres requis:
 * - agentId: ID de l'agent (peut être une expression comme "${agent.id}")
 * - listeDiffusion: Liste de diffusion (peut être une expression comme "${formulaire.liste_diffusion}")
 *   Format accepté:
 *     - String avec séparateur: "element1,element2,element3" (séparateur par défaut: virgule)
 *     - List<String>: ["element1", "element2", "element3"]
 *     - Array JSON: ["element1", "element2", "element3"]
 * - separateur: (optionnel) Séparateur à utiliser (par défaut: ",")
 * 
 * Exemple dans le formulaire:
 * - Champ texte: "liste_diffusion" avec valeur "email1@example.com,email2@example.com"
 * - Ou champ JSON: "liste_diffusion" avec valeur ["email1@example.com", "email2@example.com"]
 */
@Component
public class UpdateAgentDiffusionActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(UpdateAgentDiffusionActionHandler.class);
    
    private final AgentDiffusionSpi diffusionSpi;
    private final AgentSpi agentSpi;
    
    public UpdateAgentDiffusionActionHandler(AgentDiffusionSpi diffusionSpi, AgentSpi agentSpi) {
        this.diffusionSpi = diffusionSpi;
        this.agentSpi = agentSpi;
    }
    
    @Override
    public String getActionType() {
        return "UPDATE_AGENT_DIFFUSION";
    }
    
    @Override
    public Object execute(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        UUID agentId = getUUIDParam(params, "agentId", context);
        Object listeDiffusionObj = params.get("listeDiffusion");
        String separateur = getStringParam(params, "separateur");
        
        if (agentId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de l'agent pour UPDATE_AGENT_DIFFUSION");
        }
        
        // Vérifier que l'agent existe
        AgentPersonnel agent = agentSpi.findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));
        
        // Parser la liste de diffusion
        String listeDiffusionTexte = parseListeDiffusion(listeDiffusionObj, separateur);
        
        logger.info("Création/mise à jour d'AgentDiffusion (UPDATE_AGENT_DIFFUSION) pour l'agent {} avec la liste: {}", 
                   agentId, listeDiffusionTexte);
        
        AgentDiffusion diffusion = new AgentDiffusion(listeDiffusionTexte, separateur, agentId);
        
        // Sauvegarder (le SPI gère déjà l'upsert)
        AgentDiffusion saved = diffusionSpi.save(diffusion);
        
        logger.info("AgentDiffusion créée/mise à jour avec succès pour l'agent {}", agentId);
        
        // Stocker dans le contexte
        context.setVariable("agentDiffusion", saved);
        
        return agentId;
    }
    
    @Override
    public boolean validate(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        UUID agentId = getUUIDParam(params, "agentId", context);
        if (agentId == null) {
            logger.warn("Impossible de déterminer l'ID de l'agent pour UPDATE_AGENT_DIFFUSION");
            return false;
        }
        
        // Vérifier que l'agent existe
        if (agentSpi.findById(agentId).isEmpty()) {
            logger.warn("Agent non trouvé avec l'ID: {} pour UPDATE_AGENT_DIFFUSION", agentId);
            return false;
        }
        
        // La listeDiffusion est optionnelle (peut être vide)
        return true;
    }
    
    @SuppressWarnings("unchecked")
    private String parseListeDiffusion(Object listeDiffusionObj, String separateur) {
        String sep = separateur != null && !separateur.isBlank() ? separateur : ",";
        
        if (listeDiffusionObj == null) {
            return "";
        }
        
        // Si c'est déjà une String, la retourner telle quelle
        if (listeDiffusionObj instanceof String str) {
            if (str.isBlank()) {
                return "";
            }
            return str;
        }
        
        // Si c'est une List, la convertir en String avec le séparateur
        if (listeDiffusionObj instanceof List) {
            List<String> liste = (List<String>) listeDiffusionObj;
            if (liste.isEmpty()) {
                return "";
            }
            return String.join(sep, liste);
        }
        
        // Si c'est un Array (Object[]), le convertir en List puis en String
        if (listeDiffusionObj instanceof Object[] array) {
            List<String> liste = new ArrayList<>();
            for (Object obj : array) {
                if (obj != null) {
                    liste.add(obj.toString());
                }
            }
            if (liste.isEmpty()) {
                return "";
            }
            return String.join(sep, liste);
        }
        
        logger.warn("Format de liste de diffusion invalide: {}. Utilisation d'une chaîne vide.", 
                   listeDiffusionObj.getClass());
        return "";
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
    
    private String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }
}
