package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.dto.processus.entree.CreateProcessusCommand;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.processus.CreateProcessusFromTemplateApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentDirectionSpi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Agent.model.Role;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.Tache.model.TacheContenu;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateDependance;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.StatutProcessusJpaRepository;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.RoleEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.RoleJpaRepository;
import com.cese.process_entree_sortie.application.service.rule.RuleEngine;
import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CreateProcessusFromTemplateService implements CreateProcessusFromTemplateApi {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateProcessusFromTemplateService.class);
    
    private final ProcessusSpi processusSpi;
    private final TemplateProcessusSpi templateProcessusSpi;
    private final ProcessusConverter processusConverter;
    private final AgentSpi agentSpi;
    private final AgentDirectionSpi agentDirectionSpi;
    private final GroupeTacheSpi groupeTacheSpi;
    private final TacheSpi tacheSpi;
    private final DependanceSpi dependanceSpi;
    private final TemplateTacheSpi templateTacheSpi;
    private final StatutProcessusJpaRepository statutProcessusJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final com.cese.process_entree_sortie.application.service.tache.ActiverTachesSansDependanceService activerTachesSansDependanceService;
    private final RuleEngine ruleEngine;
    
    public CreateProcessusFromTemplateService(
            ProcessusSpi processusSpi, 
            TemplateProcessusSpi templateProcessusSpi,
            ProcessusConverter processusConverter,
            AgentSpi agentSpi,
            AgentDirectionSpi agentDirectionSpi,
            GroupeTacheSpi groupeTacheSpi,
            TacheSpi tacheSpi,
            DependanceSpi dependanceSpi,
            TemplateTacheSpi templateTacheSpi,
            StatutProcessusJpaRepository statutProcessusJpaRepository,
            RoleJpaRepository roleJpaRepository,
            com.cese.process_entree_sortie.application.service.tache.ActiverTachesSansDependanceService activerTachesSansDependanceService,
            RuleEngine ruleEngine) {
        this.processusSpi = processusSpi;
        this.templateProcessusSpi = templateProcessusSpi;
        this.processusConverter = processusConverter;
        this.agentSpi = agentSpi;
        this.agentDirectionSpi = agentDirectionSpi;
        this.groupeTacheSpi = groupeTacheSpi;
        this.tacheSpi = tacheSpi;
        this.dependanceSpi = dependanceSpi;
        this.templateTacheSpi = templateTacheSpi;
        this.statutProcessusJpaRepository = statutProcessusJpaRepository;
        this.roleJpaRepository = roleJpaRepository;
        this.activerTachesSansDependanceService = activerTachesSansDependanceService;
        this.ruleEngine = ruleEngine;
    }
    
    @Override
    public ProcessusDTO createProcessu(CreateProcessusCommand command) {
        TemplateProcessus template = templateProcessusSpi.findById(command.templateProcessusId())
            .orElseThrow(() -> new RuntimeException("Template processus non trouvé avec l'ID: " + command.templateProcessusId()));
        
        Map<String, Object> formulaireData = command.formulaireData();
        UUID agentId = command.agentId();
        
        // 2. Toujours démarrer au premier statut (la transition se fera automatiquement si nécessaire)
        List<StatutProcessus> statuts = template.statusList();
        if (statuts == null || statuts.isEmpty()) {
            throw new RuntimeException("Aucun statut trouvé pour le template");
        }
        StatutProcessus premierStatut = statuts.get(0);
        UUID statutInitialId = premierStatut.id(); // Toujours le premier statut
        logger.info("Processus démarré au premier statut: {} (codeStatut: {}, libStatut: {})", 
            statutInitialId, premierStatut.codeStatut(), premierStatut.libStatut());
        
        // 3. Extraire la direction depuis le formulaire si disponible
        UUID directionConcerneeId = extraireDirectionId(formulaireData);
        
        // 4. Vérifier si une action CREATE_AGENT existe dans les actions
        boolean aActionCreateAgent = false;
        if (formulaireData != null && !formulaireData.isEmpty()) {
            aActionCreateAgent = verifierSiActionCreateAgentExiste(template, premierStatut);
        }
        
        // 5. Si formulaireData est fourni, créer l'agent OU exécuter les actions
        if (formulaireData != null && !formulaireData.isEmpty()) {
            if (aActionCreateAgent) {
                // Si CREATE_AGENT existe, exécuter les actions d'abord (l'action créera l'agent)
                // On passe null pour agentId car il sera créé par l'action
                try {
                    RuleExecutionContext context = executerActionsAvantCreationProcessus(
                        template, formulaireData, null, directionConcerneeId);
                    // Récupérer l'agentId depuis le contexte après l'exécution
                    Object agentIdObj = context.getVariable("agentId");
                    if (agentIdObj instanceof UUID) {
                        agentId = (UUID) agentIdObj;
                        logger.info("Agent créé par l'action CREATE_AGENT avec l'ID: {}", agentId);
                    } else {
                        throw new RuntimeException("L'action CREATE_AGENT n'a pas retourné d'agentId valide");
                    }
                } catch (Exception e) {
                    logger.error("Erreur lors de l'exécution des actions avant la création du processus: {}", e.getMessage(), e);
                    throw new RuntimeException("Erreur lors de l'exécution des actions: " + e.getMessage(), e);
                }
            } else {
                // Si CREATE_AGENT n'existe pas, créer l'agent manuellement
                agentId = creerAgentPersonnelDepuisFormulaire(formulaireData, template.type());
                logger.info("Agent créé depuis le formulaire avec l'ID: {} et l'état: {}", agentId, template.type());
                
                // Puis exécuter les autres actions (s'il y en a)
                try {
                    executerActionsAvantCreationProcessus(template, formulaireData, agentId, directionConcerneeId);
                    logger.info("Toutes les actions ont été exécutées avec succès avant la création du processus");
                } catch (Exception e) {
                    logger.error("Erreur lors de l'exécution des actions avant la création du processus: {}", e.getMessage(), e);
                    throw new RuntimeException("Erreur lors de l'exécution des actions: " + e.getMessage(), e);
                }
            }
        }
        
        if (agentId == null) {
            throw new RuntimeException("agentId est requis pour créer un processus");
        }
        
        // 6. Calculer la date d'échéance du processus selon le type
        LocalDate dateEcheanceProcessus = calculerDateEcheanceProcessus(template.type(), formulaireData, command.dateDebut());
        
        // 7. Créer le processus
        InstanceProcessus nouveauProcessus = new InstanceProcessus(
            UUID.randomUUID(),
            template.codeProcessus(),
            LocalDateTime.now(),
            dateEcheanceProcessus,
            directionConcerneeId,
            agentId,
            command.templateProcessusId(),
            statutInitialId,
            new ArrayList<>(), // Sera rempli après
            new ArrayList<>(), // Sera rempli après
            template.type()
        );
        
        InstanceProcessus processusSauvegarde = processusSpi.save(nouveauProcessus);
        logger.info("Processus créé avec l'ID: {} et statutId: {}", processusSauvegarde.id(), processusSauvegarde.statutId());
        
        // Vérifier que le statut a bien le bon codeStatut pour apparaître dans le dashboard
        statutProcessusJpaRepository.findById(statutInitialId).ifPresentOrElse(
            statut -> {
                logger.info("Statut utilisé - ID: {}, codeStatut: '{}', libStatut: '{}'", 
                    statut.getId(), statut.getCodeStatut(), statut.getLibStatut());
                if (!"en_attente".equals(statut.getCodeStatut()) && !"en_cours".equals(statut.getCodeStatut())) {
                    logger.warn("ATTENTION: Le processus ne s'affichera PAS dans le dashboard car le codeStatut est '{}' au lieu de 'en_attente' ou 'en_cours'", 
                        statut.getCodeStatut());
                } else {
                    logger.info("Le processus devrait apparaître dans le dashboard (codeStatut: '{}')", statut.getCodeStatut());
                }
            },
            () -> logger.error("ERREUR: Statut avec ID {} non trouvé dans la base de données!", statutInitialId)
        );
        
        // 8. Créer les instances (groupes, tâches, dépendances) avec la date d'échéance du processus
        creerInstancesDepuisTemplate(processusSauvegarde, template, formulaireData, dateEcheanceProcessus);
        
        // 9. Vérifier si un groupe a isDirectionConcernee=true et mettre à jour la direction du processus si nécessaire
        InstanceProcessus processusAvecDirectionMiseAJour = mettreAJourDirectionSiNecessaire(processusSauvegarde, template);
        
        // 10. Synchroniser les statuts des groupes (si des tâches sont déjà faites)
        // et avancer le statut du processus si le premier statut est déjà terminé
        InstanceProcessus processusAvecStatutMiseAJour = synchroniserGroupesEtAvancerStatutSiNecessaire(
            processusAvecDirectionMiseAJour,
            template
        );
        
        // 11. Activer automatiquement les tâches et groupes sans dépendances du statut courant
        try {
            activerTachesSansDependanceService.activerTachesSansDependance(processusAvecStatutMiseAJour.id());
        } catch (Exception e) {
            logger.warn("Erreur lors de l'activation automatique des tâches pour le nouveau processus {}: {}", 
                       processusAvecStatutMiseAJour.id(), e.getMessage());
        }
        
        // 12. Recharger le processus complet
        InstanceProcessus processusComplet = processusSpi.findById(processusAvecStatutMiseAJour.id())
            .orElseThrow(() -> new RuntimeException("Processus non trouvé après création"));
        
        return processusConverter.convertirEnDTO(processusComplet);
    }

    private InstanceProcessus synchroniserGroupesEtAvancerStatutSiNecessaire(
            InstanceProcessus processus,
            TemplateProcessus template) {
        List<InstanceGroupeTache> groupes = groupeTacheSpi.findByProcessusId(processus.id());
        if (groupes == null || groupes.isEmpty()) {
            return processus;
        }
        
        // Mettre à jour le statut des groupes en fonction des tâches
        List<InstanceGroupeTache> groupesMisAJour = new ArrayList<>();
        for (InstanceGroupeTache groupe : groupes) {
            InstanceGroupeTache groupeMisAJour = groupe.changerStatut();
            if (!groupeMisAJour.statut().equals(groupe.statut())) {
                groupeMisAJour = groupeTacheSpi.save(groupeMisAJour);
            }
            groupesMisAJour.add(groupeMisAJour);
        }
        
        // Déterminer le statut actuel
        List<StatutProcessus> statuts = template.statusList();
        if (statuts == null || statuts.isEmpty()) {
            return processus;
        }
        
        StatutProcessus statutActuel = statuts.stream()
            .filter(s -> s.id().equals(processus.statutId()))
            .findFirst()
            .orElse(null);
        if (statutActuel == null) {
            logger.warn("Statut actuel {} non trouvé dans la liste des statuts du template", processus.statutId());
            return processus;
        }
        
        int indexStatutActuel = statuts.indexOf(statutActuel);
        boolean estDernierStatut = indexStatutActuel >= statuts.size() - 1;
        
        Map<UUID, UUID> statutParTemplateGroupe = template.groupeTacheList().stream()
            .collect(Collectors.toMap(TemplateGroupeTache::id, TemplateGroupeTache::statutProcessusId));
        
        List<InstanceGroupeTache> groupesDuStatutActuel = groupesMisAJour.stream()
            .filter(g -> {
                UUID statutId = statutParTemplateGroupe.get(g.templateId());
                return statutId != null && statutId.equals(statutActuel.id());
            })
            .collect(Collectors.toList());
        
        if (groupesDuStatutActuel.isEmpty()) {
            return processus;
        }
        
        boolean tousGroupesTermines = groupesDuStatutActuel.stream()
            .allMatch(g -> g.statut().equals(StatutTache.fait));
        
        if (tousGroupesTermines && !estDernierStatut) {
            StatutProcessus prochainStatut = statuts.get(indexStatutActuel + 1);
            if ("archive".equals(prochainStatut.codeStatut())) {
                logger.info("Statut suivant '{}' est archivé, on n'avance pas automatiquement (processus {})", 
                           prochainStatut.libStatut(), processus.id());
                return processus;
            }
            
            logger.info("Statut initial déjà terminé, passage au statut suivant '{}'", prochainStatut.libStatut());
            InstanceProcessus processusModifie = processus.changerStatut(prochainStatut.id());
            return processusSpi.save(processusModifie);
        }
        
        return processus;
    }
    
    /**
     * Calcule la date d'échéance du processus selon son type.
     * Pour un processus d'entrée :
     * - Si date d'entrée < aujourd'hui : aujourd'hui + 1 semaine
     * - Si date d'entrée >= aujourd'hui : date d'entrée + 1 semaine
     * Pour les autres types : utilise dateDebut si fournie, sinon aujourd'hui + 30 jours
     */
    private LocalDate calculerDateEcheanceProcessus(TypeProcessus typeProcessus, Map<String, Object> formulaireData, LocalDate dateDebut) {
        if (typeProcessus == TypeProcessus.entree && formulaireData != null) {
            // Extraire la date d'arrivée depuis le formulaire
            Object dateArriveeObj = formulaireData.get("date_arrivee");
            LocalDate dateArrivee = null;
            
            if (dateArriveeObj != null) {
                if (dateArriveeObj instanceof LocalDate) {
                    dateArrivee = (LocalDate) dateArriveeObj;
                } else if (dateArriveeObj instanceof String) {
                    try {
                        dateArrivee = LocalDate.parse((String) dateArriveeObj);
                    } catch (Exception e) {
                        logger.warn("Impossible de parser la date d'arrivée: {}", dateArriveeObj, e);
                    }
                }
            }
            
            if (dateArrivee != null) {
                LocalDate aujourdhui = LocalDate.now();
                // Si date d'entrée < aujourd'hui : aujourd'hui + 1 semaine
                // Si date d'entrée >= aujourd'hui : date d'entrée + 1 semaine
                LocalDate dateReference = dateArrivee.isBefore(aujourdhui) ? aujourdhui : dateArrivee;
                LocalDate dateEcheance = dateReference.plusWeeks(1);
                logger.info("Date d'échéance calculée pour processus d'entrée: date d'arrivée={}, date référence={}, date échéance={}", 
                    dateArrivee, dateReference, dateEcheance);
                return dateEcheance;
            }
        }
        else if (typeProcessus == TypeProcessus.sortie && formulaireData != null) {
        
                
            Object dateDepartObj = formulaireData.get("date_depart");
            LocalDate dateDepart = null;
            
            if (dateDepartObj != null) {
                if (dateDepartObj instanceof LocalDate) {
                    dateDepart = (LocalDate) dateDepartObj;
                } else if (dateDepartObj instanceof String) {
                    try {
                        dateDepart = LocalDate.parse((String) dateDepartObj);
                    } catch (Exception e) {
                        logger.warn("Impossible de parser la date d'arrivée: {}", dateDepartObj, e);
                    }
                }
            }
            
            if (dateDepart != null) {
                LocalDate aujourdhui = LocalDate.now();
                // Si date d'entrée < aujourd'hui : aujourd'hui + 1 semaine
                // Si date d'entrée >= aujourd'hui : date d'entrée + 1 semaine
                LocalDate dateReference = dateDepart.isBefore(aujourdhui) ? aujourdhui : dateDepart;
                LocalDate dateEcheance = dateReference.plusMonths(3);
                logger.info("Date d'échéance calculée pour processus de sortie: date de départ={}, date référence={}, date échéance={}", 
                    dateDepart, dateReference, dateEcheance);
                return dateEcheance;
            }
        }
        // Pour les autres types ou si pas de date d'arrivée : utiliser dateDebut si fournie, sinon aujourd'hui + 30 jours
        return dateDebut != null ? dateDebut : LocalDate.now().plusDays(30);
    }
    
    private UUID creerAgentPersonnelDepuisFormulaire(Map<String, Object> formulaireData, TypeProcessus typeProcessus) {
        String nom = (String) formulaireData.get("nom_agent");
        String prenom = (String) formulaireData.get("prenom_agent");
        String email = (String) formulaireData.get("email_agent");
        Object roleValue = formulaireData.get("role");
        
        if (nom == null || prenom == null || email == null || roleValue == null) {
            throw new RuntimeException("Données du formulaire incomplètes pour créer l'agent");
        }
        
        // Vérifier si l'agent existe déjà par email
        Optional<AgentPersonnel> agentExistant = agentSpi.findByEmail(email);
        if (agentExistant.isPresent()) {
            logger.info("Agent existant trouvé avec l'email: {}, mise à jour de l'état selon le type de processus", email);
            AgentPersonnel agent = agentExistant.get();
            
            // Déterminer l'état de l'agent en fonction du type de processus
            EtatAgent nouvelEtat;
            switch (typeProcessus) {
                case entree:
                    nouvelEtat = EtatAgent.entree;
                    break;
                case mobitliteInterne: // Note: faute de frappe dans l'enum TypeProcessus
                    nouvelEtat = EtatAgent.mobiliteInterne;
                    break;
                case sortie:
                    nouvelEtat = EtatAgent.sortie;
                    break;
                default:
                    nouvelEtat = EtatAgent.entree; // Par défaut
                    logger.warn("Type de processus inconnu: {}, utilisation de l'état 'entree' par défaut", typeProcessus);
            }
            
            // Mettre à jour l'état de l'agent existant
            AgentPersonnel agentModifie = agent.changerEtatAgent(nouvelEtat);
            AgentPersonnel agentSauvegarde = agentSpi.save(agentModifie);
            logger.info("État de l'agent mis à jour: {} pour le type de processus: {}", nouvelEtat, typeProcessus);
            return agentSauvegarde.id();
        }
        
        // Si l'agent n'existe pas, créer un nouvel agent
        Role role;
        try {
            // Détecter si roleValue est un UUID (String) ou un nom de rôle
            String roleStr = roleValue.toString();
            
            // Essayer de parser comme UUID
            UUID roleId;
            try {
                roleId = UUID.fromString(roleStr);
                // Si c'est un UUID, récupérer le RoleEntity et convertir en enum
                RoleEntity roleEntity = roleJpaRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
                
                // Convertir la valeur de RoleEntity vers l'enum Role
                role = switch (roleEntity.getValeur()) {
                    case 1 -> Role.Agent;
                    case 2 -> Role.Manager;
                    case 3 -> Role.Admin;
                    case 4 -> Role.Prestataire;
                    case 5 -> Role.Conseiller;
                    default -> throw new RuntimeException("Valeur de rôle inconnue: " + roleEntity.getValeur());
                };
                logger.info("Rôle converti depuis UUID {} vers {}", roleId, role);
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
                role = Role.valueOf(roleEnumName);
                logger.info("Rôle converti depuis nom '{}' vers {}", roleStr, role);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide: " + roleValue, e);
        }
        
        // Déterminer l'état de l'agent en fonction du type de processus
        EtatAgent etatAgent;
        switch (typeProcessus) {
            case entree:
                etatAgent = EtatAgent.entree;
                break;
            case mobitliteInterne: // Note: faute de frappe dans l'enum TypeProcessus
                etatAgent = EtatAgent.mobiliteInterne;
                break;
            case sortie:
                etatAgent = EtatAgent.sortie;
                break;
            default:
                etatAgent = EtatAgent.entree; // Par défaut
                logger.warn("Type de processus inconnu: {}, utilisation de l'état 'entree' par défaut", typeProcessus);
        }
        
        UUID agentId = UUID.randomUUID();
        AgentPersonnel agent = new AgentPersonnel(
            agentId,
            nom,
            prenom,
            email,
            role,
            etatAgent,
            new ArrayList<>(), // agentDirections vide au départ
            new ArrayList<>(), // agentAffectations vide au départ
            null // agentMaterielEtDroit null au départ
        );
        
        AgentPersonnel agentSauvegarde = agentSpi.save(agent);
        logger.info("Nouvel agent créé avec l'état: {} pour le type de processus: {}", etatAgent, typeProcessus);
        return agentSauvegarde.id();
    }
    
    private UUID extraireDirectionId(Map<String, Object> formulaireData) {
        if (formulaireData == null) {
            return null;
        }
        
        Object directionObj = formulaireData.get("direction");
        if (directionObj instanceof String) {
            try {
                return UUID.fromString((String) directionObj);
            } catch (IllegalArgumentException e) {
                logger.warn("Direction fournie comme String invalide (pas un UUID): {}", directionObj);
                return null;
            }
        }
        // If it's already a UUID (e.g., if the frontend sends it directly as UUID object, though unlikely with JSON)
        if (directionObj instanceof UUID) {
            return (UUID) directionObj;
        }
        
        logger.warn("Direction fournie dans un format inattendu: {}", directionObj);
        return null;
    }
    
    private void creerInstancesDepuisTemplate(
            InstanceProcessus processus, 
            TemplateProcessus template,
            Map<String, Object> formulaireData,
            LocalDate dateEcheanceProcessus) {
        
        // Maps pour stocker les correspondances template -> instance
        Map<UUID, UUID> templateGroupeToInstance = new HashMap<>();
        Map<UUID, UUID> templateTacheToInstance = new HashMap<>();
        
        // 1. Créer les groupes de tâches
        for (TemplateGroupeTache templateGroupe : template.groupeTacheList()) {
            InstanceGroupeTache instanceGroupe = creerInstanceGroupeTache(processus, templateGroupe, dateEcheanceProcessus);
            InstanceGroupeTache groupeSauvegarde = groupeTacheSpi.save(instanceGroupe);
            templateGroupeToInstance.put(templateGroupe.id(), groupeSauvegarde.id());
            
            // 2. Créer les tâches du groupe
            List<TemplateTache> templateTaches = templateTacheSpi.findByTemplateGroupeId(templateGroupe.id());
            List<InstanceTache> instanceTaches = new ArrayList<>();
            
            for (TemplateTache templateTache : templateTaches) {
                InstanceTache instanceTache = creerInstanceTache(
                    processus, 
                    groupeSauvegarde, 
                    templateTache,
                    formulaireData,
                    templateGroupe.statutProcessusId(), // Statut du groupe pour identifier si c'est le premier statut
                    dateEcheanceProcessus // Date d'échéance max du processus
                );
                
                InstanceTache tacheSauvegarde = tacheSpi.save(instanceTache);
                instanceTaches.add(tacheSauvegarde);
                templateTacheToInstance.put(templateTache.id(), tacheSauvegarde.id());
            }
            
            // Mettre à jour le groupe avec ses tâches (en conservant les agents assignés)
            InstanceGroupeTache groupeAvecTaches = new InstanceGroupeTache(
                groupeSauvegarde.id(),
                groupeSauvegarde.dateEcheance(),
                groupeSauvegarde.code(),
                groupeSauvegarde.libelle(),
                groupeSauvegarde.statut(),
                groupeSauvegarde.templateId(),
                groupeSauvegarde.processusId(),
                instanceTaches,
                groupeSauvegarde.agentAssigneIdList()
            );
            groupeTacheSpi.save(groupeAvecTaches);
        }
        
        // 3. Créer les dépendances (nouvelle structure : 1 source → N cibles)
        // Grouper les dépendances template par source
        Map<UUID, List<TemplateDependance>> dependancesParSource = new HashMap<>();
        for (TemplateDependance templateDependance : template.dependanceList()) {
            UUID sourceId = templateDependance.sourceTacheId();
            dependancesParSource.computeIfAbsent(sourceId, k -> new ArrayList<>()).add(templateDependance);
        }
        
        // Créer une dépendance instance par source avec toutes ses cibles
        Map<UUID, UUID> dependanceIdParCible = new HashMap<>(); // Map pour associer chaque tâche cible à son dependanceId
        for (Map.Entry<UUID, List<TemplateDependance>> entry : dependancesParSource.entrySet()) {
            UUID sourceTemplateId = entry.getKey();
            List<TemplateDependance> dependancesDeCetteSource = entry.getValue();
            
            // Trouver l'ID instance de la source
            UUID sourceInstanceId = templateTacheToInstance.get(sourceTemplateId);
            if (sourceInstanceId == null) {
                logger.warn("Impossible de trouver l'instance source pour la dépendance template {}", sourceTemplateId);
                continue;
            }
            
            // Collecter toutes les tâches cibles
            List<UUID> cibleTacheInstanceIds = new ArrayList<>();
            for (TemplateDependance templateDep : dependancesDeCetteSource) {
                for (UUID cibleTemplateId : templateDep.cibleTacheIds()) {
                    UUID cibleInstanceId = templateTacheToInstance.get(cibleTemplateId);
                    if (cibleInstanceId != null) {
                        cibleTacheInstanceIds.add(cibleInstanceId);
                        // Associer cette tâche cible à la dépendance qu'on va créer
                        dependanceIdParCible.put(cibleInstanceId, null); // Sera mis à jour après création
                    }
                }
            }
            
            if (!cibleTacheInstanceIds.isEmpty()) {
                // Créer la dépendance instance
                UUID dependanceId = UUID.randomUUID();
                InstanceDependance instanceDependance = new InstanceDependance(
                    dependanceId,
                    sourceInstanceId,
                    cibleTacheInstanceIds,
                    template.dependanceList().get(0).id() // Utiliser le premier templateId comme référence
                );
                InstanceDependance dependanceSauvegardee = dependanceSpi.save(instanceDependance);
                
                // Mettre à jour la map pour associer chaque cible à son dependanceId
                for (UUID cibleInstanceId : cibleTacheInstanceIds) {
                    dependanceIdParCible.put(cibleInstanceId, dependanceSauvegardee.id());
                }
            }
        }
        
        // 4. Mettre à jour les tâches cibles avec leur dependanceId
        for (Map.Entry<UUID, UUID> entry : dependanceIdParCible.entrySet()) {
            UUID tacheInstanceId = entry.getKey();
            UUID dependanceId = entry.getValue();
            
            if (dependanceId != null) {
                Optional<InstanceTache> tacheOpt = tacheSpi.findById(tacheInstanceId);
                if (tacheOpt.isPresent()) {
                    InstanceTache tache = tacheOpt.get();
                    // Créer une nouvelle instance avec le dependanceId
                    InstanceTache tacheAvecDependance = new InstanceTache(
                        tache.id(),
                        tache.code(),
                        tache.libelle(),
                        tache.contenu(),
                        tache.statut(),
                        tache.dateEcheance(),
                        tache.templateId(),
                        tache.groupeTacheId(),
                        dependanceId
                    );
                    tacheSpi.save(tacheAvecDependance);
                }
            }
        }
        
        logger.info("Toutes les instances créées pour le processus {}", processus.id());
    }
    
    /**
     * Met à jour la direction du processus si au moins un groupe a isDirectionConcernee=true.
     * Dans ce cas, on utilise la direction de l'agent (depuis AgentDirection) au lieu de celle du template.
     */
    private InstanceProcessus mettreAJourDirectionSiNecessaire(
            InstanceProcessus processus, 
            TemplateProcessus template) {
        
        // Vérifier si au moins un groupe a isDirectionConcernee=true
        boolean aGroupeDirectionConcernee = template.groupeTacheList().stream()
            .anyMatch(TemplateGroupeTache::isDirectionConcernee);
        
        if (!aGroupeDirectionConcernee) {
            return processus; // Pas de changement nécessaire
        }
        
        // Récupérer la direction de l'agent depuis AgentDirection
        UUID directionAgent = agentDirectionSpi.findByAgentId(processus.agentId())
            .stream()
            .findFirst()
            .map(ad -> ad.directionId())
            .orElse(null);
        
        if (directionAgent == null) {
            logger.warn("Aucune AgentDirection trouvée pour l'agent {}, impossible de mettre à jour la direction du processus", 
                       processus.agentId());
            return processus;
        }
        
        // Si la direction est différente, mettre à jour le processus
        if (!directionAgent.equals(processus.directionConcerneeId())) {
            logger.info("Mise à jour de la direction du processus {} de {} vers {} (direction de l'agent)", 
                       processus.id(), processus.directionConcerneeId(), directionAgent);
            
            // Créer un nouveau processus avec la direction mise à jour
            InstanceProcessus processusModifie = new InstanceProcessus(
                processus.id(),
                processus.codeProcessus(),
                processus.dateCreation(),
                processus.dateEcheance(),
                directionAgent, // Nouvelle direction
                processus.agentId(),
                processus.templateId(),
                processus.statutId(),
                processus.groupeTachesList(),
                processus.dependances(),
                processus.typeProcessus()
            );
            
            return processusSpi.save(processusModifie);
        }
        
        return processus;
    }
    
    private InstanceGroupeTache creerInstanceGroupeTache(
            InstanceProcessus processus, 
            TemplateGroupeTache templateGroupe,
            LocalDate dateEcheanceProcessus) {
        
        // Calculer la date d'échéance par défaut (30 jours), mais ne pas dépasser celle du processus
        LocalDate dateEcheanceBase = LocalDate.now().plusDays(30);
        LocalDate dateEcheance = dateEcheanceBase.isAfter(dateEcheanceProcessus) ? dateEcheanceProcessus : dateEcheanceBase;
        
        // Pour les groupes de tâches, il faut au moins un agent assigné
        // On utilise l'agent du processus par défaut
        List<UUID> agentAssigneIdList = new ArrayList<>();
        if (processus.agentId() != null) {
            agentAssigneIdList.add(processus.agentId());
        } else {
            // Si pas d'agent, on doit en assigner un (récupérer depuis la direction du groupe)
            // Pour l'instant, on lance une exception
            throw new RuntimeException("Aucun agent assigné au processus, impossible de créer le groupe de tâches");
        }
        
        // Note: La logique isDirectionConcernee est gérée dans mettreAJourDirectionSiNecessaire
        // car on doit d'abord créer l'AgentDirection via les règles
        
        return new InstanceGroupeTache(
            UUID.randomUUID(),
            dateEcheance,
            templateGroupe.codeTemplate(),
            templateGroupe.libGroupTache(),
            StatutTache.enAttente,
            templateGroupe.id(),
            processus.id(),
            new ArrayList<>(), // Tâches seront ajoutées après
            agentAssigneIdList
        );
    }
    
    private InstanceTache creerInstanceTache(
            InstanceProcessus processus,
            InstanceGroupeTache groupe,
            TemplateTache templateTache,
            Map<String, Object> formulaireData,
            UUID statutGroupeId,
            LocalDate dateEcheanceProcessus) {
        
        UUID tacheId = UUID.randomUUID();
        // Calculer la date d'échéance de base selon le délai du template
        LocalDate dateEcheanceBase = LocalDate.now().plusDays(templateTache.delaijour() > 0 ? templateTache.delaijour() : 30);
        // Limiter à la date d'échéance du processus (ne pas dépasser)
        LocalDate dateEcheance = dateEcheanceBase.isAfter(dateEcheanceProcessus) ? dateEcheanceProcessus : dateEcheanceBase;
        
        // Déterminer le contenu initial
        TacheContenu contenu = templateTache.contenu();
        // Les tâches commencent toujours à "à faire" (pas enAttente)
        StatutTache statutInitial = StatutTache.aFaire;
        
        // Vérifier si c'est le premier statut du processus
        TemplateProcessus template = templateProcessusSpi.findById(processus.templateId())
            .orElseThrow(() -> new RuntimeException("Template non trouvé"));
        
        List<StatutProcessus> statuts = template.statusList();
        boolean estPremierStatut = !statuts.isEmpty() && statuts.get(0).id().equals(statutGroupeId);
        
        // Si c'est le premier statut ET formulaireData est fourni, copier les données et marquer comme fait
        if (estPremierStatut && formulaireData != null && !formulaireData.isEmpty()) {
            // Copier les données du formulaire dans le contenu
            contenu = copierDonneesFormulaire(templateTache.contenu(), formulaireData);
            // Marquer la tâche comme faite car le formulaire a déjà été rempli
            statutInitial = StatutTache.fait;
            logger.info("Première tâche détectée, données du formulaire copiées et tâche marquée comme faite");
        }
        
        InstanceTache instanceTache = new InstanceTache(
            tacheId,
            templateTache.code(),
            templateTache.libelle(),
            contenu,
            statutInitial,
            dateEcheance,
            templateTache.id(),
            groupe.id(),
            templateTache.dependanceId() // Le dependanceId sera mis à jour après création des dépendances
        );
        
        return instanceTache;
    }
    
    private TacheContenu copierDonneesFormulaire(TacheContenu contenuTemplate, Map<String, Object> formulaireData) {
        // Créer une nouvelle Map avec les données du formulaire
        Map<String, Object> nouveauContenu = new HashMap<>();
        
        // Si le template a une structure "champs", on la préserve
        if (contenuTemplate != null && contenuTemplate.contenuTache() != null) {
            nouveauContenu.putAll(contenuTemplate.contenuTache());
        }
        
        // Ajouter les valeurs du formulaire
        nouveauContenu.putAll(formulaireData);
        
        return new TacheContenu(nouveauContenu);
    }
    
    /**
     * Vérifie si une action CREATE_AGENT existe dans les actions du formulaire.
     * 
     * @param template Le template du processus
     * @param premierStatut Le premier statut du processus
     * @return true si CREATE_AGENT existe dans les actions, false sinon
     */
    private boolean verifierSiActionCreateAgentExiste(TemplateProcessus template, StatutProcessus premierStatut) {
        // Trouver le premier groupe de tâches du premier statut
        Optional<TemplateGroupeTache> premierGroupeOpt = template.groupeTacheList().stream()
            .filter(groupe -> groupe.statutProcessusId().equals(premierStatut.id()))
            .findFirst();
        
        if (premierGroupeOpt.isEmpty()) {
            return false;
        }
        
        TemplateGroupeTache premierGroupe = premierGroupeOpt.get();
        
        // Trouver la première tâche du groupe avec un formulaire (contenu avec actions)
        List<TemplateTache> templateTaches = templateTacheSpi.findByTemplateGroupeId(premierGroupe.id());
        Optional<TemplateTache> tacheAvecActionsOpt = templateTaches.stream()
            .filter(tache -> {
                TacheContenu contenu = tache.contenu();
                if (contenu == null || contenu.contenuTache() == null) {
                    return false;
                }
                return contenu.contenuTache().containsKey("actions");
            })
            .findFirst();
        
        if (tacheAvecActionsOpt.isEmpty()) {
            return false;
        }
        
        TemplateTache templateTache = tacheAvecActionsOpt.get();
        TacheContenu contenu = templateTache.contenu();
        
        // Extraire les actions
        Object actionsObj = contenu.contenuTache().get("actions");
        if (actionsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> actions = (List<Map<String, Object>>) actionsObj;
            
            // Vérifier si CREATE_AGENT existe
            return actions.stream()
                .anyMatch(action -> "CREATE_AGENT".equals(action.get("type")));
        }
        
        return false;
    }
    
    /**
     * Exécute les actions définies dans la première tâche du premier statut AVANT la création du processus.
     * Les actions sont exécutées une par une avec rollback en cas d'erreur.
     * 
     * @param template Le template du processus
     * @param formulaireData Les données du formulaire
     * @param agentId L'ID de l'agent créé (peut être null si CREATE_AGENT existe dans les actions)
     * @param directionConcerneeId L'ID de la direction concernée
     * @return Le contexte d'exécution après l'exécution des actions (pour récupérer l'agentId si créé par CREATE_AGENT)
     */
    private RuleExecutionContext executerActionsAvantCreationProcessus(
            TemplateProcessus template,
            Map<String, Object> formulaireData,
            UUID agentId,
            UUID directionConcerneeId) {
        
        // Trouver la première tâche du premier statut
        List<StatutProcessus> statuts = template.statusList();
        if (statuts == null || statuts.isEmpty()) {
            logger.debug("Aucun statut trouvé, pas d'actions à exécuter");
            // Retourner un contexte vide
            UUID agentIdPourProcessusTemporaire = agentId != null ? agentId : UUID.randomUUID();
            UUID directionIdPourProcessusTemporaire = directionConcerneeId != null ? directionConcerneeId : UUID.randomUUID();
            InstanceProcessus processusTemporaire = new InstanceProcessus(
                UUID.randomUUID(),
                template.codeProcessus(),
                LocalDateTime.now(),
                LocalDate.now().plusDays(30),
                directionIdPourProcessusTemporaire,
                agentIdPourProcessusTemporaire,
                template.id(),
                UUID.randomUUID(),
                new ArrayList<>(),
                new ArrayList<>(),
                template.type()
            );
            InstanceTache tacheTemporaire = new InstanceTache(
                UUID.randomUUID(),
                "",
                "",
                new TacheContenu(Map.of()),
                StatutTache.fait,
                LocalDate.now().plusDays(30),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null
            );
            return new RuleExecutionContext(formulaireData, processusTemporaire, tacheTemporaire);
        }
        
        StatutProcessus premierStatut = statuts.get(0);
        
        // Trouver le premier groupe de tâches du premier statut
        Optional<TemplateGroupeTache> premierGroupeOpt = template.groupeTacheList().stream()
            .filter(groupe -> groupe.statutProcessusId().equals(premierStatut.id()))
            .findFirst();
        
        if (premierGroupeOpt.isEmpty()) {
            logger.debug("Aucun groupe de tâches trouvé pour le premier statut, pas d'actions à exécuter");
            // Retourner un contexte vide
            UUID agentIdPourProcessusTemporaire = agentId != null ? agentId : UUID.randomUUID();
            UUID directionIdPourProcessusTemporaire = directionConcerneeId != null ? directionConcerneeId : UUID.randomUUID();
            InstanceProcessus processusTemporaire = new InstanceProcessus(
                UUID.randomUUID(),
                template.codeProcessus(),
                LocalDateTime.now(),
                LocalDate.now().plusDays(30),
                directionIdPourProcessusTemporaire,
                agentIdPourProcessusTemporaire,
                template.id(),
                premierStatut.id(),
                new ArrayList<>(),
                new ArrayList<>(),
                template.type()
            );
            InstanceTache tacheTemporaire = new InstanceTache(
                UUID.randomUUID(),
                "",
                "",
                new TacheContenu(Map.of()),
                StatutTache.fait,
                LocalDate.now().plusDays(30),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null
            );
            return new RuleExecutionContext(formulaireData, processusTemporaire, tacheTemporaire);
        }
        
        TemplateGroupeTache premierGroupe = premierGroupeOpt.get();
        
        // Trouver la première tâche du groupe avec un formulaire (contenu avec actions)
        List<TemplateTache> templateTaches = templateTacheSpi.findByTemplateGroupeId(premierGroupe.id());
        Optional<TemplateTache> tacheAvecActionsOpt = templateTaches.stream()
            .filter(tache -> {
                TacheContenu contenu = tache.contenu();
                if (contenu == null || contenu.contenuTache() == null) {
                    return false;
                }
                return contenu.contenuTache().containsKey("actions");
            })
            .findFirst();
        
        if (tacheAvecActionsOpt.isEmpty()) {
            logger.debug("Aucune tâche avec actions trouvée dans le premier groupe, pas d'actions à exécuter");
            // Retourner un contexte vide si aucune action
            InstanceProcessus processusTemporaire = new InstanceProcessus(
                UUID.randomUUID(),
                template.codeProcessus(),
                LocalDateTime.now(),
                LocalDate.now().plusDays(30),
                directionConcerneeId,
                agentId,
                template.id(),
                premierStatut.id(),
                new ArrayList<>(),
                new ArrayList<>(),
                template.type()
            );
            InstanceTache tacheTemporaire = new InstanceTache(
                UUID.randomUUID(),
                "",
                "",
                new TacheContenu(Map.of()),
                StatutTache.fait,
                LocalDate.now().plusDays(30),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null
            );
            return new RuleExecutionContext(formulaireData, processusTemporaire, tacheTemporaire);
        }
        
        TemplateTache templateTache = tacheAvecActionsOpt.get();
        
        // Créer un processus temporaire pour le contexte (ne sera pas sauvegardé)
        // Si agentId ou directionConcerneeId sont null, utiliser des UUID temporaires
        // Le vrai agentId sera récupéré depuis le contexte après l'exécution des actions
        UUID agentIdPourProcessusTemporaire = agentId != null ? agentId : UUID.randomUUID();
        UUID directionIdPourProcessusTemporaire = directionConcerneeId != null ? directionConcerneeId : UUID.randomUUID();
        InstanceProcessus processusTemporaire = new InstanceProcessus(
            UUID.randomUUID(),
            template.codeProcessus(),
            LocalDateTime.now(),
            LocalDate.now().plusDays(30),
            directionIdPourProcessusTemporaire,
            agentIdPourProcessusTemporaire,
            template.id(),
            premierStatut.id(),
            new ArrayList<>(),
            new ArrayList<>(),
            template.type()
        );
        
        // Créer une tâche temporaire avec le contenu du template (incluant les actions)
        // et les données du formulaire
        TacheContenu contenuTemporaire = copierDonneesFormulaire(templateTache.contenu(), formulaireData);
        InstanceTache tacheTemporaire = new InstanceTache(
            UUID.randomUUID(),
            templateTache.code(),
            templateTache.libelle(),
            contenuTemporaire,
            StatutTache.fait, // Marquer comme fait car le formulaire a été rempli
            LocalDate.now().plusDays(30),
            templateTache.id(),
            UUID.randomUUID(), // Groupe temporaire (non utilisé)
            null // Pas de dépendance
        );
        
        // Créer le contexte d'exécution
        RuleExecutionContext context = new RuleExecutionContext(
            formulaireData,
            processusTemporaire,
            tacheTemporaire
        );
        
        // Exécuter les actions (avec rollback automatique en cas d'erreur)
        logger.info("Exécution des actions avant la création du processus pour la tâche template {}", templateTache.id());
        ruleEngine.executeRules(tacheTemporaire, context);
        logger.info("Toutes les actions ont été exécutées avec succès");
        
        return context;
    }
    
}