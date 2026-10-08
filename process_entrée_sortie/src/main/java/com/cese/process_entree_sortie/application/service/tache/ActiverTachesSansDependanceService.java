package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour activer automatiquement les tâches et groupes de tâches sans dépendances
 * lorsqu'un processus change de statut.
 */
@Service
@Transactional
public class ActiverTachesSansDependanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(ActiverTachesSansDependanceService.class);
    
    private final ProcessusSpi processusSpi;
    private final TemplateProcessusSpi templateProcessusSpi;
    private final GroupeTacheSpi groupeTacheSpi;
    private final TacheSpi tacheSpi;
    private final DependanceSpi dependanceSpi;
    
    public ActiverTachesSansDependanceService(
            ProcessusSpi processusSpi,
            TemplateProcessusSpi templateProcessusSpi,
            GroupeTacheSpi groupeTacheSpi,
            TacheSpi tacheSpi,
            DependanceSpi dependanceSpi) {
        this.processusSpi = processusSpi;
        this.templateProcessusSpi = templateProcessusSpi;
        this.groupeTacheSpi = groupeTacheSpi;
        this.tacheSpi = tacheSpi;
        this.dependanceSpi = dependanceSpi;
    }
    
    /**
     * Active automatiquement les tâches et groupes de tâches sans dépendances
     * pour le statut actuel du processus.
     * 
     * @param processusId L'ID du processus
     */
    public void activerTachesSansDependance(UUID processusId) {
        try {
            InstanceProcessus processus = processusSpi.findById(processusId)
                .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'ID: " + processusId));
            
            TemplateProcessus template = templateProcessusSpi.findById(processus.templateId())
                .orElseThrow(() -> new RuntimeException("Template processus non trouvé"));
            
            // Récupérer tous les groupes et tâches du processus
            List<InstanceGroupeTache> tousGroupes = groupeTacheSpi.findByProcessusId(processusId);
            List<TemplateGroupeTache> templatesGroupes = template.groupeTacheList();
            
            // Filtrer les groupes qui appartiennent au statut actuel
            List<InstanceGroupeTache> groupesDuStatutActuel = tousGroupes.stream()
                .filter(g -> {
                    TemplateGroupeTache templateGroupe = templatesGroupes.stream()
                        .filter(tg -> tg.id().equals(g.templateId()))
                        .findFirst()
                        .orElse(null);
                    
                    return templateGroupe != null && 
                           templateGroupe.statutProcessusId().equals(processus.statutId());
                })
                .collect(Collectors.toList());
            
            logger.info("Activation automatique des tâches/groupes sans dépendances pour le processus {} au statut {}", 
                       processusId, processus.statutId());
            
            // Pour chaque groupe du statut actuel
            for (InstanceGroupeTache groupe : groupesDuStatutActuel) {
                // Les groupes n'ont plus de dépendances (seules les tâches en ont)
                // Si le groupe est en attente, l'activer
                if (groupe.statut().equals(StatutTache.enAttente)) {
                    logger.info("Activation automatique du groupe {} (pas de dépendances)", groupe.id());
                    InstanceGroupeTache groupeActive = groupe.changerStatut(); // Passe de enAttente à enCours
                    groupeTacheSpi.save(groupeActive);
                }
                
                // Pour chaque tâche du groupe
                for (InstanceTache tache : groupe.tacheList()) {
                    // Vérifier si la tâche a une dépendance (via dependanceId)
                    // Si dependanceId est null, la tâche n'a pas de dépendance
                    boolean tacheADependance = tache.dependanceId() != null;
                    
                    if (tacheADependance) {
                        // La tâche a une dépendance, vérifier si la source est terminée
                        Optional<InstanceDependance> dependanceOpt = dependanceSpi.findById(tache.dependanceId());
                        if (dependanceOpt.isPresent()) {
                            InstanceDependance dependance = dependanceOpt.get();
                            // Trouver la tâche source
                            Optional<InstanceTache> sourceTacheOpt = tacheSpi.findById(dependance.sourceTacheId());
                            if (sourceTacheOpt.isPresent() && sourceTacheOpt.get().statut().equals(StatutTache.fait)) {
                                // La source est terminée, démarrer la tâche (aFaire -> enCours) si elle est à "à faire"
                                if (tache.statut().equals(StatutTache.aFaire)) {
                                    logger.info("Démarrage de la tâche {} (dépendance satisfaite)", tache.id());
                                    InstanceTache tacheEnCours = tache.demarer(); // aFaire -> enCours
                                    tacheSpi.save(tacheEnCours);
                                }
                            }
                        }
                    } else {
                        // La tâche n'a pas de dépendance, la démarrer directement (aFaire -> enCours) si elle est à "à faire"
                        if (tache.statut().equals(StatutTache.aFaire)) {
                            logger.info("Démarrage direct de la tâche {} (pas de dépendances)", tache.id());
                            InstanceTache tacheEnCours = tache.demarer(); // aFaire -> enCours
                            tacheSpi.save(tacheEnCours);
                        }
                    }
                }
            }
            
            logger.info("Activation automatique terminée pour le processus {}", processusId);
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'activation automatique des tâches pour le processus {}: {}", 
                        processusId, e.getMessage(), e);
            // Ne pas faire échouer la transaction si l'activation automatique échoue
        }
    }
}
