package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.CompleteGroupeTacheApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentDirectionSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDirection;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompleteGroupeTacheService implements CompleteGroupeTacheApi {
    
    private static final Logger logger = LoggerFactory.getLogger(CompleteGroupeTacheService.class);
    
    private final GroupeTacheSpi groupeTacheSpi;
    private final GroupeTacheConverter converter;
    private final ProcessusSpi processusSpi;
    private final TemplateProcessusSpi templateProcessusSpi;
    private final ActiverTachesSansDependanceService activerTachesSansDependanceService;
    private final com.cese.process_entree_sortie.application.service.notification.NotificationService notificationService;
    private final AgentSpi agentSpi;
    private final AgentDirectionSpi agentDirectionSpi;
    
    public CompleteGroupeTacheService(
            GroupeTacheSpi groupeTacheSpi, 
            GroupeTacheConverter converter,
            ProcessusSpi processusSpi,
            TemplateProcessusSpi templateProcessusSpi,
            ActiverTachesSansDependanceService activerTachesSansDependanceService,
            com.cese.process_entree_sortie.application.service.notification.NotificationService notificationService,
            AgentSpi agentSpi,
            AgentDirectionSpi agentDirectionSpi) {
        this.groupeTacheSpi = groupeTacheSpi;
        this.converter = converter;
        this.processusSpi = processusSpi;
        this.templateProcessusSpi = templateProcessusSpi;
        this.activerTachesSansDependanceService = activerTachesSansDependanceService;
        this.notificationService = notificationService;
        this.agentSpi = agentSpi;
        this.agentDirectionSpi = agentDirectionSpi;
    }
    
    @Override
    public GroupeTacheDTO completeGroupeTacheApi(UUID groupeTacheId) {
        InstanceGroupeTache groupe = groupeTacheSpi.findById(groupeTacheId)
            .orElseThrow(() -> new RuntimeException("Groupe tache non trouvé avec l'ID: " + groupeTacheId));
        
        // Utiliser changerStatut() qui met à jour le statut si toutes les tâches sont faites
        InstanceGroupeTache groupeModifie = groupe.changerStatut();
        InstanceGroupeTache groupeSauvegarde = groupeTacheSpi.save(groupeModifie);
        
        // Si le groupe est maintenant terminé, vérifier si on peut passer au statut suivant
        if (groupeSauvegarde.statut().equals(StatutTache.fait)) {
            verifierEtAvancerStatutProcessus(groupeSauvegarde.processusId());
            
            // Vérifier si la direction a terminé tous ses groupes et envoyer une notification
            TemplateProcessus template = templateProcessusSpi.findById(
                processusSpi.findById(groupeSauvegarde.processusId())
                    .orElseThrow(() -> new RuntimeException("Processus non trouvé"))
                    .templateId()
            ).orElseThrow(() -> new RuntimeException("Template non trouvé"));
            
            TemplateGroupeTache templateGroupe = template.groupeTacheList().stream()
                .filter(tg -> tg.id().equals(groupeSauvegarde.templateId()))
                .findFirst()
                .orElse(null);
            
            if (templateGroupe != null) {
                notificationService.verifierEtNotifierDirectionTerminee(
                    groupeSauvegarde.processusId(), 
                    templateGroupe.directionId()
                );
            }
        }
        
        return converter.convertirEnDTO(groupeSauvegarde);
    }
    
    private void verifierEtAvancerStatutProcessus(UUID processusId) {
        InstanceProcessus processus = processusSpi.findById(processusId)
            .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'ID: " + processusId));
        
        // Récupérer le template pour avoir la liste des statuts
        TemplateProcessus template = templateProcessusSpi.findById(processus.templateId())
            .orElseThrow(() -> new RuntimeException("Template processus non trouvé"));
        
        List<StatutProcessus> statuts = template.statusList();
        if (statuts == null || statuts.isEmpty()) {
            logger.warn("Aucun statut trouvé pour le template, impossible d'avancer");
            return;
        }
        
        // Trouver le statut actuel
        StatutProcessus statutActuel = statuts.stream()
            .filter(s -> s.id().equals(processus.statutId()))
            .findFirst()
            .orElse(null);
        
        if (statutActuel == null) {
            logger.warn("Statut actuel {} non trouvé dans la liste des statuts du template", processus.statutId());
            return;
        }
        
        int indexStatutActuel = statuts.indexOf(statutActuel);
        boolean estDernierStatut = indexStatutActuel >= statuts.size() - 1;
        StatutProcessus statutArchive = statuts.stream()
            .filter(s -> "archive".equals(s.codeStatut()))
            .findFirst()
            .orElse(null);
        StatutProcessus dernierStatutFonctionnel = statuts.stream()
            .filter(s -> !"archive".equals(s.codeStatut()))
            .reduce((first, second) -> second)
            .orElse(null);
        
        // Récupérer tous les groupes du processus
        List<InstanceGroupeTache> tousGroupes = groupeTacheSpi.findByProcessusId(processusId);
        
        // Récupérer les templates groupes pour connaître leur statut
        List<TemplateGroupeTache> templatesGroupes = template.groupeTacheList();
        
        // Filtrer les groupes qui appartiennent au statut actuel
        List<InstanceGroupeTache> groupesDuStatutActuel = tousGroupes.stream()
            .filter(g -> {
                // Trouver le template groupe pour connaître son statut
                TemplateGroupeTache templateGroupe = templatesGroupes.stream()
                    .filter(tg -> tg.id().equals(g.templateId()))
                    .findFirst()
                    .orElse(null);
                
                return templateGroupe != null && 
                       templateGroupe.statutProcessusId().equals(statutActuel.id());
            })
            .collect(Collectors.toList());
        
        // Si le statut n'a aucun groupe, on ne peut pas avancer
        if (groupesDuStatutActuel.isEmpty()) {
            logger.debug("Le statut '{}' n'a aucun groupe de tâches, pas d'avancement", statutActuel.libStatut());
            return;
        }
        
        // Vérifier si tous les groupes du statut actuel sont terminés
        boolean tousGroupesTermines = groupesDuStatutActuel.stream()
            .allMatch(g -> g.statut().equals(StatutTache.fait));
        
        // Si tous les groupes sont terminés ET qu'il y a un statut suivant
        if (tousGroupesTermines && !estDernierStatut) {
            StatutProcessus prochainStatut = statuts.get(indexStatutActuel + 1);

            if ("archive".equals(prochainStatut.codeStatut())) {
                logger.info("Statut suivant '{}' est archivé, on n'avance pas automatiquement (processus {})", prochainStatut.libStatut(), processusId);
                // Vérifier quand même le changement d'état de l'agent
                verifierEtChangerEtatAgentSiNecessaire(processus);
                // Archiver si la date de mobilité est atteinte
                archiverProcessusSiDateAtteinte(processus, prochainStatut);
                return;
            }
            
            logger.info("Tous les groupes du statut '{}' sont terminés, passage au statut suivant '{}'", 
                       statutActuel.libStatut(), prochainStatut.libStatut());
            
            // Mettre à jour le processus avec le nouveau statut
            InstanceProcessus processusModifie = processus.changerStatut(prochainStatut.id());
            processusSpi.save(processusModifie);
            
            logger.info("Processus {} avancé au statut {}", processusId, prochainStatut.libStatut());
            
            // Activer automatiquement les tâches et groupes sans dépendances du nouveau statut
            activerTachesSansDependanceService.activerTachesSansDependance(processusId);
            
            // Si on vient de passer au dernier statut fonctionnel, vérifier l'archivage immédiat
            if (dernierStatutFonctionnel != null
                && prochainStatut.id().equals(dernierStatutFonctionnel.id())
                && statutArchive != null) {
                verifierEtChangerEtatAgentSiNecessaire(processusModifie);
                archiverProcessusSiDateAtteinte(processusModifie, statutArchive);
            }
            
            // Important: ne pas rappeler verifierEtAvancerStatutProcessus ici
        } else if (tousGroupesTermines && estDernierStatut) {
            // Tous les groupes sont terminés ET on est au dernier statut
            // Vérifier si on doit changer l'état de l'agent selon la date
            verifierEtChangerEtatAgentSiNecessaire(processus);
            if (statutArchive != null) {
                archiverProcessusSiDateAtteinte(processus, statutArchive);
            }
        } else {
            logger.debug("Pas tous les groupes du statut actuel sont terminés, pas d'avancement");
        }
    }
    
    /**
     * Vérifie si on est au dernier statut et la date de mobilisation est atteinte,
     * puis change l'état de l'agent au statut suivant selon le type de processus :
     * - entree → actif
     * - mobitliteInterne → actif
     * - sortie → ancien
     */
    private void verifierEtChangerEtatAgentSiNecessaire(InstanceProcessus processus) {
        try {
            logger.info("Vérif changement état agent (processus {}, type {})", processus.id(), processus.typeProcessus());
            AgentPersonnel agent = agentSpi.findById(processus.agentId())
                .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + processus.agentId()));
            
            LocalDate aujourdhui = LocalDate.now();
            boolean doitChangerEtat = false;
            EtatAgent nouvelEtat = null;
            
            LocalDate dateMobilite = null;
            if (processus.typeProcessus() == TypeProcessus.entree || processus.typeProcessus() == TypeProcessus.mobitliteInterne) {
                dateMobilite = agentDirectionSpi.findCurrentByAgentId(agent.id())
                    .map(ad -> ad.dateArrivee())
                    .orElse(null);
            } else if (processus.typeProcessus() == TypeProcessus.sortie) {
                dateMobilite = agentDirectionSpi.findByAgentId(agent.id())
                    .stream()
                    .filter(ad -> ad.dateDepart() != null)
                    .findFirst()
                    .map(ad -> ad.dateDepart())
                    .orElse(null);
            }

            logger.info("Date mobilité trouvée: {} (aujourd'hui: {})", dateMobilite, aujourdhui);
            
            if (dateMobilite != null && !dateMobilite.isAfter(aujourdhui)) {
                if (processus.typeProcessus() == TypeProcessus.entree && agent.etatAgent() == EtatAgent.entree) {
                    doitChangerEtat = true;
                    nouvelEtat = EtatAgent.actif;
                    logger.info("Date de mobilisation ({}) atteinte pour l'agent {}, passage de 'entree' à 'actif'", 
                               dateMobilite, agent.id());
                } else if (processus.typeProcessus() == TypeProcessus.mobitliteInterne && agent.etatAgent() == EtatAgent.mobiliteInterne) {
                    doitChangerEtat = true;
                    nouvelEtat = EtatAgent.actif;
                    logger.info("Date de mobilisation ({}) atteinte pour l'agent {}, passage de 'mobiliteInterne' à 'actif'", 
                               dateMobilite, agent.id());
                } else if (processus.typeProcessus() == TypeProcessus.sortie && agent.etatAgent() == EtatAgent.sortie) {
                    doitChangerEtat = true;
                    nouvelEtat = EtatAgent.ancien;
                    logger.info("Date de mobilisation ({}) atteinte pour l'agent {}, passage de 'sortie' à 'ancien'", 
                               dateMobilite, agent.id());
                }
            }
            
            if (doitChangerEtat && nouvelEtat != null) {
                AgentPersonnel agentModifie = agent.changerEtatAgent(nouvelEtat);
                agentSpi.save(agentModifie);
                logger.info("État de l'agent {} changé de '{}' à '{}'", agent.id(), agent.etatAgent(), nouvelEtat);
            } else {
                logger.info("Aucun changement d'état agent requis (etatAgent={}, typeProcessus={})", agent.etatAgent(), processus.typeProcessus());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification et changement d'état de l'agent pour le processus {}: {}", 
                        processus.id(), e.getMessage(), e);
            // Ne pas bloquer le processus en cas d'erreur
        }
    }

    private void archiverProcessusSiDateAtteinte(InstanceProcessus processus, StatutProcessus statutArchive) {
        try {
            logger.info("Tentative d'archivage processus {} avec statut {}", processus.id(), statutArchive.libStatut());
            LocalDate aujourdhui = LocalDate.now();
            LocalDate dateMobilite = null;

            if (processus.typeProcessus() == TypeProcessus.entree || processus.typeProcessus() == TypeProcessus.mobitliteInterne) {
                dateMobilite = agentDirectionSpi.findCurrentByAgentId(processus.agentId())
                    .map(AgentDirection::dateArrivee)
                    .orElse(null);
            } else if (processus.typeProcessus() == TypeProcessus.sortie) {
                dateMobilite = agentDirectionSpi.findByAgentId(processus.agentId())
                    .stream()
                    .filter(ad -> ad.dateDepart() != null)
                    .findFirst()
                    .map(AgentDirection::dateDepart)
                    .orElse(null);
            }

            if (dateMobilite == null || dateMobilite.isAfter(aujourdhui)) {
                logger.info("Archivage ignoré: dateMobilite={} (aujourd'hui={})", dateMobilite, aujourdhui);
                return;
            }

            InstanceProcessus processusArchive = processus.changerStatut(statutArchive.id());
            processusSpi.save(processusArchive);
            logger.info("Processus {} archivé (statut {})", processus.id(), statutArchive.libStatut());
        } catch (Exception e) {
            logger.warn("Erreur lors de l'archivage du processus {}: {}", processus.id(), e.getMessage());
        }
    }
}
