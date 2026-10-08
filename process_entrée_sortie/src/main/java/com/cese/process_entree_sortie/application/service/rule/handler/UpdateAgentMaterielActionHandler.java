package com.cese.process_entree_sortie.application.service.rule.handler;

import com.cese.process_entree_sortie.application.dto.rule.ActionRuleDTO;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.cese.process_entree_sortie.application.port.out.agent.AgentMaterielSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.rule.ActionHandler;
import com.cese.process_entree_sortie.domain.Agent.model.AgentMaterielEtDroit;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handler pour créer ou mettre à jour un AgentMaterielEtDroit depuis les données du formulaire (UPSERT).
 * Utilise la même logique que CreateAgentMaterielActionHandler mais pour le type UPDATE_AGENT_MATERIEL.
 * 
 * Paramètres requis:
 * - agentId: ID de l'agent (peut être une expression comme "${agent.id}")
 * - materielData: Données du matériel (peut être une expression comme "${formulaire.materiel}")
 *   Formats acceptés:
 *   - Liste de strings: ["ordinateur", "badge", "telephone"]
 *   - Map avec statuts: {"ordinateur": "en_possession", "badge": "rendu"} → seuls les noms sont conservés
 *   - Map mixte: {"ordinateur": "en_possession", "badge": null} → seuls les noms sont conservés
 */
@Component
public class UpdateAgentMaterielActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(UpdateAgentMaterielActionHandler.class);
    
    private final AgentMaterielSpi materielSpi;
    private final AgentSpi agentSpi;
    private final ObjectMapper objectMapper;
    
    public UpdateAgentMaterielActionHandler(
            AgentMaterielSpi materielSpi, 
            AgentSpi agentSpi,
            ObjectMapper objectMapper) {
        this.materielSpi = materielSpi;
        this.agentSpi = agentSpi;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getActionType() {
        return "UPDATE_AGENT_MATERIEL";
    }
    
    @Override
    public Object execute(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        UUID agentId = getUUIDParam(params, "agentId", context);
        Object materielDataObj = params.get("materielData");
        
        if (agentId == null) {
            throw new RuntimeException("Impossible de déterminer l'ID de l'agent pour UPDATE_AGENT_MATERIEL");
        }
        
        // Vérifier que l'agent existe
        AgentPersonnel agent = agentSpi.findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));
        
        // Parser le JSON du matériel
        logger.info("=== UPDATE_AGENT_MATERIEL pour l'agent {} ===", agentId);
        logger.info("materielDataObj reçu (type: {}): {}", 
            materielDataObj != null ? materielDataObj.getClass().getSimpleName() : "null",
            materielDataObj);
        
        Map<String, Object> materielMap = parseMaterielData(materielDataObj);
        boolean materielsProvided = hasMaterielsInput(materielDataObj);
        boolean droitsProvided = hasDroitsInput(materielDataObj);
        materielMap = mergeWithExistingIfMissing(agentId, materielMap, materielsProvided, droitsProvided);
        
        logger.info("Map parsée pour l'agent {} avec {} entrées", agentId, materielMap.size());
        logger.info("Structure de la Map parsée:");
        for (Map.Entry<String, Object> entry : materielMap.entrySet()) {
            logger.info("  - Clé '{}': valeur = {} (type: {})", 
                entry.getKey(), 
                entry.getValue(), 
                entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null");
        }
        
        AgentMaterielEtDroit materiel = new AgentMaterielEtDroit(materielMap);
        
        // Sauvegarder (le SPI gère déjà l'upsert)
        AgentMaterielEtDroit saved = materielSpi.save(agentId, materiel);
        logger.info("=== FIN UPDATE_AGENT_MATERIEL pour l'agent {} ===", agentId);
        
        // Mettre à jour l'agent pour ajouter le matériel
        AgentPersonnel agentModifie = agent.changerMaterielEtDroit(saved);
        agentSpi.save(agentModifie);
        
        logger.info("AgentMaterielEtDroit créé/mis à jour avec succès pour l'agent {}", agentId);
        
        // Stocker dans le contexte
        context.setVariable("agentMateriel", saved);
        
        return agentId;
    }
    
    @Override
    public boolean validate(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        UUID agentId = getUUIDParam(params, "agentId", context);
        if (agentId == null) {
            logger.warn("Impossible de déterminer l'ID de l'agent pour UPDATE_AGENT_MATERIEL");
            return false;
        }
        
        // Vérifier que l'agent existe
        if (agentSpi.findById(agentId).isEmpty()) {
            logger.warn("Agent non trouvé avec l'ID: {} pour UPDATE_AGENT_MATERIEL", agentId);
            return false;
        }
        
        // Le materielData est optionnel (peut être vide)
        return true;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMaterielData(Object materielDataObj) {
        if (materielDataObj == null) {
            return new HashMap<>();
        }
        
        logger.debug("Parsing materielData de type: {}", materielDataObj.getClass().getSimpleName());
        logger.debug("Valeur de materielData: {}", materielDataObj);
        
        // Si c'est une liste de strings (format du formulaire de type "liste")
        if (materielDataObj instanceof List) {
            List<?> liste = (List<?>) materielDataObj;
            List<String> materielsList = normalizeStringList(liste);
            Map<String, Object> result = new HashMap<>();
            result.put("materiels", materielsList);
            result.put("droits", List.of());
            logger.info("Conversion de {} éléments de liste en format {materiels, droits}", materielsList.size());
            return result;
        }
        
        // Si c'est déjà une Map
        if (materielDataObj instanceof Map) {
            Map<String, Object> mapOriginale = new HashMap<>((Map<String, Object>) materielDataObj);
            Map<String, Object> result = new HashMap<>();
            List<String> materielsList = new java.util.ArrayList<>();
            List<String> droitsList = new java.util.ArrayList<>();

            Object materielObj = mapOriginale.getOrDefault("materiels", mapOriginale.get("materials"));
            if (materielObj == null) {
                materielObj = mapOriginale.getOrDefault("materiel", mapOriginale.get("material"));
            }
            Object droitsObj = mapOriginale.getOrDefault("droits", mapOriginale.get("droit"));
            if (droitsObj == null && mapOriginale.containsKey("droitAgent")) {
                droitsObj = mapOriginale.get("droitAgent");
            }

            if (materielObj instanceof Map) {
                Map<String, Object> rawMateriel = (Map<String, Object>) materielObj;
                for (Map.Entry<String, Object> entry : rawMateriel.entrySet()) {
                    if (entry.getKey() != null) {
                        materielsList.add(entry.getKey().toString().trim());
                    }
                }
            } else if (materielObj instanceof List) {
                materielsList.addAll(normalizeStringList((List<?>) materielObj));
            } else if (materielObj instanceof String materielStr && !materielStr.isBlank()) {
                materielsList.addAll(parseListFromString(materielStr));
            } else if (!mapOriginale.containsKey("materiels") && !mapOriginale.containsKey("materials")
                && !mapOriginale.containsKey("materiel") && !mapOriginale.containsKey("material")) {
                for (Map.Entry<String, Object> entry : mapOriginale.entrySet()) {
                    String key = entry.getKey();
                    if (isReservedKey(key)) {
                        continue;
                    }
                    materielsList.add(key.trim());
                }
            }

            if (droitsObj instanceof List) {
                List<?> rawDroits = (List<?>) droitsObj;
                for (Object item : rawDroits) {
                    if (item != null) {
                        droitsList.add(item.toString());
                    }
                }
            } else if (droitsObj instanceof String droitStr && !droitStr.isBlank()) {
                droitsList.addAll(parseListFromString(droitStr));
            }

            result.put("materiels", normalizeStringList(materielsList));
            result.put("droits", droitsList);
            logger.info("Map normalisée en format {materiels, droits} (materiels: {}, droits: {})", materielsList.size(), droitsList.size());
            return result;
        }
        
        // Si c'est une String JSON, la parser
        if (materielDataObj instanceof String jsonString) {
            if (jsonString.isBlank() || jsonString.trim().equals("{}") || jsonString.trim().equals("[]")) {
                return new HashMap<>();
            }
            
            try {
                // Essayer de parser comme JSON
                Object parsed = objectMapper.readValue(jsonString, Object.class);
                
                // Si c'est une liste après parsing, traiter comme une liste
                if (parsed instanceof List) {
                    return parseMaterielData(parsed); // Récursion pour traiter la liste
                }
                
                // Si c'est une Map après parsing, traiter comme une Map
                if (parsed instanceof Map) {
                    return parseMaterielData(parsed); // Récursion pour traiter la Map
                }
                
                // Sinon, retourner une map vide
                logger.warn("Format JSON inattendu après parsing: {}. Type: {}", jsonString, parsed.getClass());
                return new HashMap<>();
            } catch (Exception e) {
                List<String> parsed = parseListFromString(jsonString);
                if (!parsed.isEmpty()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("materiels", parsed);
                    result.put("droits", List.of());
                    logger.info("Chaîne de liste détectée pour le matériel ({} éléments).", parsed.size());
                    return result;
                }
                logger.warn("Format JSON invalide pour le matériel: {}. Utilisation d'une map vide.", jsonString);
                return new HashMap<>();
            }
        }
        
        logger.warn("Format de matériel invalide: {}. Utilisation d'une map vide.", materielDataObj.getClass());
        return new HashMap<>();
    }

    private boolean isReservedKey(String key) {
        return "droits".equals(key) || "droit".equals(key) || "droitAgent".equals(key)
            || "materiel".equals(key) || "material".equals(key)
            || "materiels".equals(key) || "materials".equals(key);
    }

    private List<String> normalizeStringList(List<?> list) {
        List<String> result = new java.util.ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object item : list) {
            if (item != null) {
                String value = item.toString().trim();
                if (!value.isBlank()) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    private List<String> parseListFromString(String raw) {
        if (raw == null) {
            return List.of();
        }
        String cleaned = raw.trim();
        if (cleaned.isBlank()) {
            return List.of();
        }
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        if (cleaned.isBlank()) {
            return List.of();
        }
        String[] parts = cleaned.split("[,\\n]+");
        List<String> result = new java.util.ArrayList<>();
        for (String part : parts) {
            if (part != null) {
                String value = part.trim();
                if (!value.isBlank()) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    private boolean hasMaterielsInput(Object materielDataObj) {
        if (materielDataObj == null) {
            return false;
        }
        if (materielDataObj instanceof List || materielDataObj instanceof String) {
            return true;
        }
        if (materielDataObj instanceof Map<?, ?> map) {
            return map.containsKey("materiels") || map.containsKey("materials")
                || map.containsKey("materiel") || map.containsKey("material");
        }
        return false;
    }

    private boolean hasDroitsInput(Object materielDataObj) {
        if (materielDataObj == null) {
            return false;
        }
        if (materielDataObj instanceof Map<?, ?> map) {
            return map.containsKey("droits") || map.containsKey("droit") || map.containsKey("droitAgent");
        }
        return false;
    }

    private Map<String, Object> mergeWithExistingIfMissing(UUID agentId,
                                                           Map<String, Object> incoming,
                                                           boolean materielsProvided,
                                                           boolean droitsProvided) {
        if (materielsProvided && droitsProvided) {
            return incoming;
        }
        return materielSpi.findByAgentId(agentId)
            .map(existing -> {
                Map<String, Object> merged = new HashMap<>(incoming);
                Map<String, Object> existingMap = existing.agentMaterileEtDroit();
                if (!materielsProvided) {
                    Object existingMateriels = existingMap.get("materiels");
                    if (existingMateriels == null) {
                        existingMateriels = existingMap.get("materiel");
                    }
                    if (existingMateriels instanceof List<?> list) {
                        merged.put("materiels", normalizeStringList(list));
                    }
                }
                if (!droitsProvided) {
                    Object existingDroits = existingMap.get("droits");
                    if (existingDroits == null) {
                        existingDroits = existingMap.get("droit");
                    }
                    if (existingDroits instanceof List<?> list) {
                        merged.put("droits", normalizeStringList(list));
                    }
                }
                return merged;
            })
            .orElse(incoming);
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
}
