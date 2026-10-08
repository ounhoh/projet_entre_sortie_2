package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceGroupeTacheEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.InstanceGroupeTacheJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cese.process_entree_sortie.application.service.tache.ActiverTachesSansDependanceService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class GroupeTacheAdapter implements GroupeTacheSpi {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupeTacheAdapter.class);
    
    private final InstanceGroupeTacheJpaRepository jpaRepository;
    private final TacheSpi tacheSpi;
    private final ProcessusSpi processusSpi;
    private final TemplateProcessusSpi templateProcessusSpi;
    private final ActiverTachesSansDependanceService activerTachesSansDependanceService;
    
    public GroupeTacheAdapter(
            InstanceGroupeTacheJpaRepository jpaRepository, 
            @Lazy TacheSpi tacheSpi,
            @Lazy ProcessusSpi processusSpi,
            TemplateProcessusSpi templateProcessusSpi,
            @Lazy ActiverTachesSansDependanceService activerTachesSansDependanceService) {
        this.jpaRepository = jpaRepository;
        this.tacheSpi = tacheSpi;
        this.processusSpi = processusSpi;
        this.templateProcessusSpi = templateProcessusSpi;
        this.activerTachesSansDependanceService = activerTachesSansDependanceService;
    }
    
    @Override
    public InstanceGroupeTache save(InstanceGroupeTache groupeTache) {
        InstanceGroupeTacheEntity entity = convertirEnEntity(groupeTache);
        InstanceGroupeTacheEntity entitySauvegardee = jpaRepository.save(entity);
        
        // Sauvegarder les tâches du groupe
        if (groupeTache.tacheList() != null) {
            groupeTache.tacheList().forEach(tacheSpi::save);
        }
        
        InstanceGroupeTache groupeSauvegarde = chargerComplet(entitySauvegardee.getId());
        
        // Si le groupe est maintenant terminé, vérifier si le processus doit avancer au statut suivant
        if (groupeSauvegarde.statut().equals(StatutTache.fait)) {
            verifierEtAvancerStatutProcessus(groupeSauvegarde.processusId());
        }
        
        return groupeSauvegarde;
    }
    
    /**
     * Vérifie si tous les groupes du statut actuel sont terminés et fait avancer le processus au statut suivant.
     */
    private void verifierEtAvancerStatutProcessus(UUID processusId) {
        try {
            Optional<InstanceProcessus> processusOpt = processusSpi.findById(processusId);
            if (processusOpt.isEmpty()) {
                return;
            }
            
            InstanceProcessus processus = processusOpt.get();
            
            // Récupérer le template pour avoir la liste des statuts
            Optional<TemplateProcessus> templateOpt = templateProcessusSpi.findById(processus.templateId());
            if (templateOpt.isEmpty()) {
                logger.warn("Template processus non trouvé pour {}", processus.templateId());
                return;
            }
            
            TemplateProcessus template = templateOpt.get();
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
            
            // Si on est déjà au dernier statut, ne rien faire
            if (indexStatutActuel >= statuts.size() - 1) {
                logger.debug("Processus déjà au dernier statut, pas d'avancement possible");
                return;
            }
            
            // Récupérer tous les groupes du processus
            List<InstanceGroupeTache> tousGroupes = findByProcessusId(processusId);
            
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
            
            // Si le statut n'a aucun groupe, on ne doit pas avancer
            if (groupesDuStatutActuel.isEmpty()) {
                logger.debug("Le statut '{}' n'a aucun groupe de tâches, pas d'avancement", statutActuel.libStatut());
                return;
            }
            
            // Vérifier si tous les groupes du statut actuel sont terminés
            boolean tousGroupesTermines = groupesDuStatutActuel.stream()
                .allMatch(g -> g.statut().equals(StatutTache.fait));
            
            // Si tous les groupes sont terminés ET qu'il y a un statut suivant
            if (tousGroupesTermines && indexStatutActuel < statuts.size() - 1) {
                StatutProcessus prochainStatut = statuts.get(indexStatutActuel + 1);
                
                logger.info("Tous les groupes du statut '{}' sont terminés, passage au statut suivant '{}'", 
                           statutActuel.libStatut(), prochainStatut.libStatut());
                
                // Mettre à jour le processus avec le nouveau statut
                InstanceProcessus processusModifie = processus.changerStatut(prochainStatut.id());
                processusSpi.save(processusModifie);
                
                logger.info("Processus {} avancé automatiquement au statut {}", processusId, prochainStatut.libStatut());
                
                // Activer automatiquement les tâches et groupes sans dépendances du nouveau statut
                activerTachesSansDependanceService.activerTachesSansDependance(processusId);
                
                // Important: ne pas rappeler verifierEtAvancerStatutProcessus ici
            }
        } catch (Exception e) {
            // Ne pas faire échouer la sauvegarde du groupe si la mise à jour du processus échoue
            logger.warn("Erreur lors de la mise à jour automatique du processus {}: {}", processusId, e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstanceGroupeTache> findById(UUID groupeTacheId) {
        return jpaRepository.findById(groupeTacheId)
            .map(e -> chargerComplet(e.getId()));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceGroupeTache> findByProcessusId(UUID processusId) {
        return jpaRepository.findByProcessusId(processusId).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceGroupeTache> findByAgentId(UUID agentId) {
        return jpaRepository.findByAgentId(agentId).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceGroupeTache> findByStatutAndAgent(StatutTache statut, UUID agentId) {
        return jpaRepository.findByStatutAndAgent(statut, agentId).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceGroupeTache> findByStatut(StatutTache statut) {
        return jpaRepository.findByStatut(statut).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceGroupeTache> findByStatutAndAgentAndDirection(StatutTache statut, UUID agentId, UUID directionId) {
        return jpaRepository.findByStatutAndAgentAndDirection(statut, agentId, directionId).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceGroupeTache> findEnRetardByAgent(UUID agentId) {
        return jpaRepository.findEnRetardByAgent(agentId).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(InstanceGroupeTache groupeTache) {
        jpaRepository.deleteById(groupeTache.id());
    }
    
    private InstanceGroupeTacheEntity convertirEnEntity(InstanceGroupeTache groupe) {
        return new InstanceGroupeTacheEntity(
            groupe.id(),
            groupe.dateEcheance(),
            groupe.code(),
            groupe.libelle(),
            groupe.statut(),
            groupe.templateId(),
            groupe.processusId(),
            groupe.agentAssigneIdList()
        );
    }
    
    private InstanceGroupeTache chargerComplet(UUID groupeId) {
        InstanceGroupeTacheEntity entity = jpaRepository.findById(groupeId)
            .orElseThrow(() -> new RuntimeException("Groupe de tâche non trouvé"));
        
        List<InstanceTache> taches = tacheSpi.findByGroupeId(groupeId);
        
        return new InstanceGroupeTache(
            entity.getId(),
            entity.getDateEcheance(),
            entity.getCode(),
            entity.getLibelle(),
            entity.getStatut(),
            entity.getTemplateId(),
            entity.getProcessusId(),
            taches,
            entity.getAgentAssigneIdList()
        );
    }
}