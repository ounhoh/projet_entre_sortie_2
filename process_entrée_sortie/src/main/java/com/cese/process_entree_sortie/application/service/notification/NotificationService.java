package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.dto.notification.NotificationDTO;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Direction.model.Direction;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour gérer les notifications sans template.
 * Trois types de notifications :
 * 1. INFO_DIRECTION_TERMINEE : Quand tous les groupes d'une direction sont terminés
 * 2. INFO_GROUPE_DISPONIBLE : Quand un groupe devient disponible (dépendances satisfaites)
 * 3. ALERTE_DELAI : Quand une tâche/groupe approche ou dépasse son délai
 */
@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final NotificationSpi notificationSpi;
    private final ProcessusSpi processusSpi;
    private final TemplateProcessusSpi templateProcessusSpi;
    private final GroupeTacheSpi groupeTacheSpi;
    private final TacheSpi tacheSpi;
    private final DependanceSpi dependanceSpi;
    private final DirectionSpi directionSpi;
    
    public NotificationService(
            NotificationSpi notificationSpi,
            ProcessusSpi processusSpi,
            TemplateProcessusSpi templateProcessusSpi,
            GroupeTacheSpi groupeTacheSpi,
            TacheSpi tacheSpi,
            DependanceSpi dependanceSpi,
            DirectionSpi directionSpi) {
        this.notificationSpi = notificationSpi;
        this.processusSpi = processusSpi;
        this.templateProcessusSpi = templateProcessusSpi;
        this.groupeTacheSpi = groupeTacheSpi;
        this.tacheSpi = tacheSpi;
        this.dependanceSpi = dependanceSpi;
        this.directionSpi = directionSpi;
    }
    
    /**
     * Vérifie si une direction a terminé tous ses groupes et envoie une notification si c'est le cas.
     * 
     * @param processusId L'ID du processus
     * @param directionId L'ID de la direction à vérifier
     */
    public void verifierEtNotifierDirectionTerminee(UUID processusId, UUID directionId) {
        try {
            InstanceProcessus processus = processusSpi.findById(processusId)
                .orElseThrow(() -> new RuntimeException("Processus non trouvé"));
            
            TemplateProcessus template = templateProcessusSpi.findById(processus.templateId())
                .orElseThrow(() -> new RuntimeException("Template processus non trouvé"));
            
            // Récupérer tous les groupes du processus
            List<InstanceGroupeTache> tousGroupes = groupeTacheSpi.findByProcessusId(processusId);
            List<TemplateGroupeTache> templatesGroupes = template.groupeTacheList();
            
            // Filtrer les groupes de cette direction
            List<InstanceGroupeTache> groupesDeLaDirection = tousGroupes.stream()
                .filter(groupe -> {
                    TemplateGroupeTache templateGroupe = templatesGroupes.stream()
                        .filter(tg -> tg.id().equals(groupe.templateId()))
                        .findFirst()
                        .orElse(null);
                    return templateGroupe != null && templateGroupe.directionId().equals(directionId);
                })
                .collect(Collectors.toList());
            
            // Vérifier si tous les groupes de la direction sont terminés
            boolean tousTermines = groupesDeLaDirection.stream()
                .allMatch(g -> g.statut().equals(StatutTache.fait));
            
            if (tousTermines && !groupesDeLaDirection.isEmpty()) {
                Direction direction = directionSpi.findById(directionId)
                    .orElseThrow(() -> new RuntimeException("Direction non trouvée"));
                
                // Récupérer tous les agents qui ont des tâches à faire dans d'autres directions
                // (pour les notifier que cette direction a terminé)
                List<UUID> agentsAvecTaches = recupererAgentsAvecTachesAutresDirections(processusId, directionId);
                
                // Envoyer la notification même s'il n'y a pas d'agents avec tâches
                // car c'est une information importante pour le processus
                NotificationDTO notification = new NotificationDTO(
                    NotificationDTO.TypeNotification.INFO_DIRECTION_TERMINEE,
                    "Direction " + direction.libDirection() + " - Tâches terminées",
                    "La direction " + direction.libDirection() + " a terminé toutes ses tâches pour le processus.",
                    processusId,
                    null,
                    null,
                    directionId,
                    agentsAvecTaches
                );
                
                notificationSpi.envoyerNotification(notification);
                logger.info("Notification envoyée : Direction {} a terminé ses tâches", direction.libDirection());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de la direction terminée pour le processus {}: {}", 
                       processusId, e.getMessage(), e);
        }
    }
    
    /**
     * Vérifie les dépendances d'une tâche terminée et notifie si un groupe d'une autre direction devient disponible.
     * 
     * @param tacheTermineeId L'ID de la tâche qui vient d'être terminée
     */
    public void verifierEtNotifierGroupeDisponible(UUID tacheTermineeId) {
        try {
            InstanceTache tacheTerminee = tacheSpi.findById(tacheTermineeId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
            
            InstanceGroupeTache groupeSource = groupeTacheSpi.findById(tacheTerminee.groupeTacheId())
                .orElseThrow(() -> new RuntimeException("Groupe source non trouvé"));
            
            InstanceProcessus processus = processusSpi.findById(groupeSource.processusId())
                .orElseThrow(() -> new RuntimeException("Processus non trouvé"));
            
            TemplateProcessus template = templateProcessusSpi.findById(processus.templateId())
                .orElseThrow(() -> new RuntimeException("Template processus non trouvé"));
            
            // Récupérer la dépendance où cette tâche est la source
            Optional<InstanceDependance> dependanceOpt = dependanceSpi.findBySourceTacheId(tacheTermineeId);
            
            if (dependanceOpt.isPresent()) {
                InstanceDependance dependance = dependanceOpt.get();
                
                // Pour chaque tâche cible de cette dépendance
                for (UUID cibleTacheId : dependance.cibleTacheIds()) {
                    InstanceTache tacheCible = tacheSpi.findById(cibleTacheId)
                        .orElseThrow(() -> new RuntimeException("Tâche cible non trouvée"));
                    
                    InstanceGroupeTache groupeCible = groupeTacheSpi.findById(tacheCible.groupeTacheId())
                        .orElseThrow(() -> new RuntimeException("Groupe cible non trouvé"));
                    
                    // Récupérer les directions des groupes
                    TemplateGroupeTache templateGroupeSource = template.groupeTacheList().stream()
                        .filter(tg -> tg.id().equals(groupeSource.templateId()))
                        .findFirst()
                        .orElse(null);
                    
                    TemplateGroupeTache templateGroupeCible = template.groupeTacheList().stream()
                        .filter(tg -> tg.id().equals(groupeCible.templateId()))
                        .findFirst()
                        .orElse(null);
                    
                    if (templateGroupeSource != null && templateGroupeCible != null) {
                        UUID directionSource = templateGroupeSource.directionId();
                        UUID directionCible = templateGroupeCible.directionId();
                        
                        // Si les directions sont différentes OU si les groupes sont différents
                        if (!directionSource.equals(directionCible) || !groupeSource.id().equals(groupeCible.id())) {
                            // Vérifier si toutes les dépendances de la tâche cible sont satisfaites
                            if (toutesDependancesSatisfaites(tacheCible)) {
                                // Envoyer notification aux agents du groupe cible
                                NotificationDTO notification = new NotificationDTO(
                                    NotificationDTO.TypeNotification.INFO_GROUPE_DISPONIBLE,
                                    "Groupe de tâches disponible",
                                    "Le groupe de tâches '" + groupeCible.libelle() + "' est maintenant disponible.",
                                    processus.id(),
                                    groupeCible.id(),
                                    null,
                                    directionCible,
                                    groupeCible.agentAssigneIdList()
                                );
                                
                                notificationSpi.envoyerNotification(notification);
                                logger.info("Notification envoyée : Groupe {} est maintenant disponible", groupeCible.libelle());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du groupe disponible pour la tâche {}: {}", 
                       tacheTermineeId, e.getMessage(), e);
        }
    }
    
    /**
     * Vérifie les délais et envoie des alertes si nécessaire.
     * 
     * @param processusId L'ID du processus à vérifier
     */
    public void verifierEtNotifierDelais(UUID processusId) {
        try {
            processusSpi.findById(processusId)
                .orElseThrow(() -> new RuntimeException("Processus non trouvé"));
            
            List<InstanceGroupeTache> tousGroupes = groupeTacheSpi.findByProcessusId(processusId);
            LocalDate aujourdhui = LocalDate.now();
            
            for (InstanceGroupeTache groupe : tousGroupes) {
                // Vérifier si le groupe est en retard ou approche du délai
                if (groupe.statut() != StatutTache.fait) {
                    LocalDate dateEcheance = groupe.dateEcheance();
                    long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(aujourdhui, dateEcheance);
                    
                    // Alerte si en retard ou si moins de 2 jours restants
                    if (joursRestants < 0 || joursRestants <= 2) {
                        String message = joursRestants < 0 
                            ? "Le groupe de tâches '" + groupe.libelle() + "' est en retard !"
                            : "Le groupe de tâches '" + groupe.libelle() + "' approche de son délai (dans " + joursRestants + " jour(s)) !";
                        
                        NotificationDTO notification = new NotificationDTO(
                            NotificationDTO.TypeNotification.ALERTE_DELAI,
                            "Alerte délai - " + groupe.libelle(),
                            message + " Il faut faire ses tâches immédiatement.",
                            processusId,
                            groupe.id(),
                            null,
                            null,
                            groupe.agentAssigneIdList()
                        );
                        
                        notificationSpi.envoyerNotification(notification);
                        logger.info("Notification d'alerte envoyée pour le groupe {}", groupe.libelle());
                    }
                }
                
                // Vérifier aussi les tâches individuelles
                for (InstanceTache tache : groupe.tacheList()) {
                    if (tache.statut() != StatutTache.fait) {
                        LocalDate dateEcheanceTache = tache.dateEcheance();
                        long joursRestantsTache = java.time.temporal.ChronoUnit.DAYS.between(aujourdhui, dateEcheanceTache);
                        
                        if (joursRestantsTache < 0 || joursRestantsTache <= 2) {
                            String message = joursRestantsTache < 0 
                                ? "La tâche '" + tache.libelle() + "' est en retard !"
                                : "La tâche '" + tache.libelle() + "' approche de son délai (dans " + joursRestantsTache + " jour(s)) !";
                            
                            NotificationDTO notification = new NotificationDTO(
                                NotificationDTO.TypeNotification.ALERTE_DELAI,
                                "Alerte délai - " + tache.libelle(),
                                message + " Il faut faire ses tâches immédiatement.",
                                processusId,
                                groupe.id(),
                                tache.id(),
                                null,
                                groupe.agentAssigneIdList()
                            );
                            
                            notificationSpi.envoyerNotification(notification);
                            logger.info("Notification d'alerte envoyée pour la tâche {}", tache.libelle());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification des délais pour le processus {}: {}", 
                       processusId, e.getMessage(), e);
        }
    }
    
    // Méthodes utilitaires
    
    /**
     * Vérifie si toutes les dépendances d'une tâche sont satisfaites.
     * Une tâche a des dépendances entrantes si son dependanceId n'est pas null.
     * Toutes les dépendances sont satisfaites si toutes les tâches sources sont terminées.
     */
    private boolean toutesDependancesSatisfaites(InstanceTache tache) {
        // Si la tâche n'a pas de dépendance (dependanceId == null), elle est disponible
        if (tache.dependanceId() == null) {
            return true;
        }
        
        // Récupérer la dépendance
        Optional<InstanceDependance> dependanceOpt = dependanceSpi.findById(tache.dependanceId());
        if (dependanceOpt.isEmpty()) {
            return true; // Pas de dépendance trouvée = disponible
        }
        
        InstanceDependance dependance = dependanceOpt.get();
        
        // Vérifier que la tâche source est terminée
        InstanceTache tacheSource = tacheSpi.findById(dependance.sourceTacheId())
            .orElse(null);
        
        if (tacheSource == null || !tacheSource.statut().equals(StatutTache.fait)) {
            return false;
        }
        
        return true;
    }
    
    private List<UUID> recupererAgentsAvecTachesAutresDirections(UUID processusId, UUID directionIdExclue) {
        // Récupérer tous les groupes du processus qui ne sont PAS de cette direction
        // et qui ont encore des tâches à faire
        List<InstanceGroupeTache> tousGroupes = groupeTacheSpi.findByProcessusId(processusId);
        TemplateProcessus template = templateProcessusSpi.findById(
            processusSpi.findById(processusId)
                .orElseThrow(() -> new RuntimeException("Processus non trouvé"))
                .templateId()
        ).orElseThrow(() -> new RuntimeException("Template non trouvé"));
        
        List<UUID> agents = new ArrayList<>();
        for (InstanceGroupeTache groupe : tousGroupes) {
            TemplateGroupeTache templateGroupe = template.groupeTacheList().stream()
                .filter(tg -> tg.id().equals(groupe.templateId()))
                .findFirst()
                .orElse(null);
            
            // Prendre les groupes d'autres directions qui ont encore des tâches à faire
            if (templateGroupe != null && !templateGroupe.directionId().equals(directionIdExclue)) {
                // Vérifier si le groupe a encore des tâches à faire
                boolean aTachesAFaire = groupe.tacheList().stream()
                    .anyMatch(t -> t.statut() != StatutTache.fait);
                
                if (aTachesAFaire) {
                    agents.addAll(groupe.agentAssigneIdList());
                }
            }
        }
        
        return agents.stream().distinct().collect(Collectors.toList());
    }
}
