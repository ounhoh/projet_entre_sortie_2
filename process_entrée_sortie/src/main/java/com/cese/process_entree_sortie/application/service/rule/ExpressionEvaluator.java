package com.cese.process_entree_sortie.application.service.rule;

import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Évaluateur d'expressions pour résoudre les valeurs ${...} dans les règles.
 * 
 * Exemples d'expressions supportées:
 * - ${formulaire.nom_agent} -> valeur du champ "nom_agent" du formulaire
 * - ${processus.agentId} -> ID de l'agent du processus
 * - ${processus.typeProcessus} -> type du processus
 * - ${agent.id} -> ID de l'agent (variable)
 * - ${agent.exists} -> true si l'agent existe (variable booléenne)
 */
@Component
public class ExpressionEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpressionEvaluator.class);
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private final ObjectMapper objectMapper;
    
    public ExpressionEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Évalue une expression simple comme "${formulaire.nom_agent}"
     */
    public Object evaluate(String expression, RuleExecutionContext context) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        
        // Si c'est une expression ${...}, la résoudre
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String path = expression.substring(2, expression.length() - 1);
            return resolvePath(path, context);
        }
        
        // Sinon, retourner la valeur telle quelle
        return expression;
    }
    
    /**
     * Résout toutes les expressions dans un objet (récursif)
     */
    public Object resolve(Object value, RuleExecutionContext context) {
        if (value == null) {
            return null;
        }
        
        // Si c'est une String avec expression
        if (value instanceof String str) {
            return resolveString(str, context);
        }
        
        // Si c'est une Map, résoudre récursivement
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> resolved = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                Object resolvedValue = resolve(entry.getValue(), context);
                resolved.put(key, resolvedValue);
            }
            return resolved;
        }
        
        // Si c'est une List, résoudre récursivement
        if (value instanceof List<?> list) {
            List<Object> resolved = new ArrayList<>();
            for (Object item : list) {
                resolved.add(resolve(item, context));
            }
            return resolved;
        }
        
        // Sinon, retourner la valeur telle quelle
        return value;
    }
    
    /**
     * Résout une chaîne qui peut contenir plusieurs expressions
     */
    private String resolveString(String str, RuleExecutionContext context) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(str);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String path = matcher.group(1);
            Object value = resolvePath(path, context);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Résout un chemin d'accès comme "formulaire.nom_agent" ou "processus.agentId"
     */
    private Object resolvePath(String path, RuleExecutionContext context) {
        if (path == null || path.isBlank()) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        if (parts.length == 0) {
            return null;
        }
        
        String root = parts[0];
        String subPath = parts.length > 1 ? String.join(".", java.util.Arrays.copyOfRange(parts, 1, parts.length)) : null;
        
        try {
            switch (root) {
                case "formulaire":
                    return getNestedValue(context.getFormulaireData(), subPath);
                    
                case "processus":
                    return getProcessusValue(context, subPath);
                    
                case "tache":
                    return getTacheValue(context, subPath);
                    
                case "agent":
                    return getVariableValue(context, "agent", subPath);
                    
                default:
                    // Chercher dans les variables
                    Object variable = context.getVariable(root);
                    if (variable != null && subPath != null) {
                        Object result = getNestedValue(variable, subPath);
                        if (result != null) {
                            return result;
                        }
                    }
                    if (variable != null) {
                        return variable;
                    }
                    
                    // Fallback: si la variable n'existe pas et que le chemin ressemble à "agentDirection.directionId",
                    // essayer de chercher directement dans formulaireData
                    // Cela permet de supporter ${agentDirection.directionId} même si CREATE_AGENT_DIRECTION n'a pas été exécuté
                    if (subPath != null && "directionId".equals(subPath)) {
                        Map<String, Object> formulaireData = context.getFormulaireData();
                        if (formulaireData != null) {
                            // Chercher "direction" dans formulaireData
                            Object directionObj = formulaireData.get("direction");
                            if (directionObj != null) {
                                logger.debug("Fallback: directionId trouvé dans formulaireData via clé 'direction' pour expression {}", path);
                                return directionObj;
                            }
                            // Chercher "directionId" dans formulaireData
                            Object directionIdObj = formulaireData.get("directionId");
                            if (directionIdObj != null) {
                                logger.debug("Fallback: directionId trouvé dans formulaireData via clé 'directionId' pour expression {}", path);
                                return directionIdObj;
                            }
                        }
                    }
                    
                    return null;
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la résolution du chemin '{}': {}", path, e.getMessage());
            return null;
        }
    }
    
    private Object getNestedValue(Object obj, String path) {
        if (obj == null || path == null || path.isBlank()) {
            return obj;
        }
        
        String[] parts = path.split("\\.");
        Object current = obj;
        
        for (String part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
            if (current == null) {
                return null;
            }
        }
        
        return current;
    }
    
    private Object getProcessusValue(RuleExecutionContext context, String subPath) {
        if (context.getProcessus() == null) {
            return null;
        }
        
        if (subPath == null || subPath.isBlank()) {
            return context.getProcessus();
        }
        
        return switch (subPath) {
            case "id" -> context.getProcessus().id();
            case "agentId" -> context.getProcessus().agentId();
            case "typeProcessus" -> context.getProcessus().typeProcessus().toString();
            case "directionConcerneeId" -> context.getProcessus().directionConcerneeId();
            default -> null;
        };
    }
    
    private Object getTacheValue(RuleExecutionContext context, String subPath) {
        if (context.getTache() == null) {
            return null;
        }
        
        if (subPath == null || subPath.isBlank()) {
            return context.getTache();
        }
        
        return switch (subPath) {
            case "id" -> context.getTache().id();
            case "code" -> context.getTache().code();
            case "libelle" -> context.getTache().libelle();
            default -> null;
        };
    }
    
    private Object getVariableValue(RuleExecutionContext context, String variableName, String subPath) {
        Object variable = context.getVariable(variableName);
        if (variable == null) {
            // Pour "agent.exists", retourner false si l'agent n'existe pas
            if ("exists".equals(subPath)) {
                return false;
            }
            return null;
        }
        
        if (subPath == null || subPath.isBlank()) {
            return variable;
        }
        
        // Si c'est une Map, chercher la clé
        if (variable instanceof Map<?, ?> map) {
            return map.get(subPath);
        }
        
        // Si c'est un objet avec des propriétés, utiliser la réflexion ou un switch
        if (subPath.equals("id") && variable instanceof UUID) {
            return variable;
        }
        
        if (subPath.equals("exists")) {
            return variable != null;
        }
        
        return null;
    }
}
