package com.cese.process_entree_sortie.application.service.rule.handler;

import com.cese.process_entree_sortie.application.dto.rule.ActionRuleDTO;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.rule.ActionHandler;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Agent.model.Role;
import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.RoleEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.RoleJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler pour créer ou mettre à jour un agent depuis les données du formulaire (UPSERT).
 * Utilise la même logique que CreateAgentActionHandler mais pour le type UPDATE_AGENT.
 * 
 * Paramètres requis:
 * - nom: Nom de l'agent
 * - prenom: Prénom de l'agent
 * - email: Email de l'agent
 * - role: Rôle de l'agent (Agent, Manager, Admin)
 * - etatAgent: État de l'agent (peut être une expression comme "${processus.typeProcessus}")
 */
@Component
public class UpdateAgentActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(UpdateAgentActionHandler.class);
    
    private final AgentSpi agentSpi;
    private final RoleJpaRepository roleJpaRepository;
    
    public UpdateAgentActionHandler(AgentSpi agentSpi, RoleJpaRepository roleJpaRepository) {
        this.agentSpi = agentSpi;
        this.roleJpaRepository = roleJpaRepository;
    }
    
    @Override
    public String getActionType() {
        return "UPDATE_AGENT";
    }
    
    @Override
    public Object execute(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        String nom = getStringParam(params, "nom");
        String prenom = getStringParam(params, "prenom");
        String email = getStringParam(params, "email");
        String roleStr = getStringParam(params, "role");
        String etatAgentStr = getStringParam(params, "etatAgent");
        
        logger.info("Création/mise à jour d'un agent (UPDATE_AGENT): {} {}, email: {}", prenom, nom, email);
        
        UUID agentId = getAgentId(context);
        AgentPersonnel agentSauvegarde;

        if (agentId != null) {
            AgentPersonnel agentExistant = agentSpi.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));

            Optional<AgentPersonnel> agentEmailOpt = agentSpi.findByEmail(email);
            if (agentEmailOpt.isPresent() && !agentEmailOpt.get().id().equals(agentId)) {
                AgentPersonnel agentEmail = agentEmailOpt.get();
                if (agentEmail.etatAgent() == EtatAgent.ancien) {
                    agentSpi.save(agentEmail.changerEtatAgent(EtatAgent.ancien));
                } else {
                    throw new RuntimeException("Adresse email déjà utilisée par un agent actif.");
                }
            }

            Role role = parseRole(roleStr);
            EtatAgent etatAgent = parseEtatAgent(etatAgentStr, context);

            AgentPersonnel agentModifie = new AgentPersonnel(
                agentExistant.id(),
                nom,
                prenom,
                email,
                role,
                etatAgent,
                agentExistant.agentDirections(),
                agentExistant.agentAffectations(),
                agentExistant.agentMaterielEtDroit()
            );

            agentSauvegarde = agentSpi.save(agentModifie);
            logger.info("Agent mis à jour avec l'ID: {} et l'état: {}", agentSauvegarde.id(), etatAgent);
        } else {
            Optional<AgentPersonnel> agentEmailOpt = agentSpi.findByEmail(email);
            if (agentEmailOpt.isPresent() && agentEmailOpt.get().etatAgent() != EtatAgent.ancien) {
                throw new RuntimeException("Adresse email déjà utilisée par un agent actif.");
            }
            if (agentEmailOpt.isPresent() && agentEmailOpt.get().etatAgent() == EtatAgent.ancien) {
                agentSpi.save(agentEmailOpt.get().changerEtatAgent(EtatAgent.ancien));
            }

            Role role = parseRole(roleStr);
            EtatAgent etatAgent = parseEtatAgent(etatAgentStr, context);
            UUID newAgentId = UUID.randomUUID();
            AgentPersonnel agent = new AgentPersonnel(
                newAgentId,
                nom,
                prenom,
                email,
                role,
                etatAgent,
                new ArrayList<>(),
                new ArrayList<>(),
                null
            );
            agentSauvegarde = agentSpi.save(agent);
            logger.info("Nouvel agent créé avec l'ID: {} et l'état: {}", agentSauvegarde.id(), etatAgent);
        }
        
        // Stocker dans le contexte pour les actions suivantes
        context.setVariable("agent", agentSauvegarde);
        context.setVariable("agentId", agentSauvegarde.id());
        
        return agentSauvegarde.id();
    }
    
    @Override
    public boolean validate(ActionRuleDTO rule, RuleExecutionContext context) {
        Map<String, Object> params = rule.params();
        
        // Vérifier les paramètres requis
        if (!params.containsKey("nom") || getStringParam(params, "nom") == null) {
            logger.warn("Paramètre 'nom' manquant pour UPDATE_AGENT");
            return false;
        }
        if (!params.containsKey("prenom") || getStringParam(params, "prenom") == null) {
            logger.warn("Paramètre 'prenom' manquant pour UPDATE_AGENT");
            return false;
        }
        if (!params.containsKey("email") || getStringParam(params, "email") == null) {
            logger.warn("Paramètre 'email' manquant pour UPDATE_AGENT");
            return false;
        }
        if (!params.containsKey("role") || getStringParam(params, "role") == null) {
            logger.warn("Paramètre 'role' manquant pour UPDATE_AGENT");
            return false;
        }
        
        return true;
    }
    
    private String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }

    private UUID getAgentId(RuleExecutionContext context) {
        Object agentId = context.getVariable("agentId");
        if (agentId instanceof UUID uuid) {
            return uuid;
        }
        Object agentObj = context.getVariable("agent");
        if (agentObj instanceof AgentPersonnel agent) {
            return agent.id();
        }
        if (context.getProcessus() != null && context.getProcessus().agentId() != null) {
            return context.getProcessus().agentId();
        }
        return null;
    }
    
    private Role parseRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            throw new IllegalArgumentException("Le rôle ne peut pas être vide");
        }
        
        try {
            // Essayer de parser comme UUID
            UUID roleId;
            try {
                roleId = UUID.fromString(roleStr);
                // Si c'est un UUID, récupérer le RoleEntity et convertir en enum
                RoleEntity roleEntity = roleJpaRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
                
                // Convertir la valeur de RoleEntity vers l'enum Role
                Role role = switch (roleEntity.getValeur()) {
                    case 1 -> Role.Agent;
                    case 2 -> Role.Manager;
                    case 3 -> Role.Admin;
                    case 4 -> Role.Prestataire;
                    case 5 -> Role.Conseiller;
                    default -> throw new RuntimeException("Valeur de rôle inconnue: " + roleEntity.getValeur());
                };
                logger.info("Rôle converti depuis UUID {} vers {}", roleId, role);
                return role;
            } catch (IllegalArgumentException e) {
                // Ce n'est pas un UUID, traiter comme un nom de rôle
                // Convertir "AGENT" -> "Agent", "MANAGER" -> "Manager", "ADMIN" -> "Admin", "PRESTATAIRE" -> "Prestataire, "CONSEILLER" -> "Conseiller"
                String roleNormalized = roleStr.toUpperCase();
                String roleEnumName = switch (roleNormalized) {
                    case "AGENT" -> "Agent";
                    case "MANAGER" -> "Manager";
                    case "ADMIN" -> "Admin";
                    case "PRESTATAIRE" -> "Prestataire";
                    case "CONSEILLER" -> "Conseiller";
                    default -> roleNormalized; // Si déjà au bon format, garder tel quel
                };
                Role role = Role.valueOf(roleEnumName);
                logger.info("Rôle converti depuis nom '{}' vers {}", roleStr, role);
                return role;
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide: " + roleStr, e);
        }
    }
    
    private EtatAgent parseEtatAgent(String etatAgentStr, RuleExecutionContext context) {
        if (etatAgentStr == null || etatAgentStr.isBlank()) {
            // Par défaut, utiliser le type de processus
            String typeProcessus = context.getTypeProcessus();
            return mapTypeProcessusToEtatAgent(typeProcessus);
        }
        
        try {
            return EtatAgent.valueOf(etatAgentStr);
        } catch (IllegalArgumentException e) {
            // Si ce n'est pas un enum valide, essayer de mapper depuis le type de processus
            return mapTypeProcessusToEtatAgent(etatAgentStr);
        }
    }
    
    private EtatAgent mapTypeProcessusToEtatAgent(String typeProcessus) {
        if (typeProcessus == null) {
            return EtatAgent.entree; // Par défaut
        }
        
        return switch (typeProcessus.toLowerCase()) {
            case "entree" -> EtatAgent.entree;
            case "mobiliteinterne", "mobitliteinterne" -> EtatAgent.mobiliteInterne;
            case "sortie" -> EtatAgent.sortie;
            default -> {
                logger.warn("Type de processus inconnu: {}, utilisation de l'état 'entree' par défaut", typeProcessus);
                yield EtatAgent.entree;
            }
        };
    }
}