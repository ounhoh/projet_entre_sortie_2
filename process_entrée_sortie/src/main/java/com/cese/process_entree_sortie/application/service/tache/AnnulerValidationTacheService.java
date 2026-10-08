package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.AnnulerValidationTacheApi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.HashSet;
import java.util.ArrayList;

@Service
@Transactional
public class AnnulerValidationTacheService implements AnnulerValidationTacheApi {
    
    private static final Logger logger = LoggerFactory.getLogger(AnnulerValidationTacheService.class);
    
    private final TacheSpi tacheSpi;
    private final GroupeTacheSpi groupeTacheSpi;
    private final TacheConverter converter;
    private final DependanceSpi dependanceSpi;

    public AnnulerValidationTacheService(TacheSpi tacheSpi, GroupeTacheSpi groupeTacheSpi, TacheConverter converter, DependanceSpi dependanceSpi) {
        this.tacheSpi = tacheSpi;
        this.groupeTacheSpi = groupeTacheSpi;
        this.converter = converter;
        this.dependanceSpi = dependanceSpi;
    }
        
    private List<InstanceTache> trouverTachesDependante(InstanceTache tache) {
        HashSet<UUID> visites = new HashSet<>();
        ArrayList<InstanceTache> resultat = new ArrayList<>();
        ArrayList<InstanceTache> pile = new ArrayList<>();
        pile.add(tache);
        visites.add(tache.id());

        while (!pile.isEmpty()) {
            InstanceTache current = pile.remove(pile.size() - 1);
            Optional<InstanceDependance> dependanceOpt = dependanceSpi.findBySourceTacheId(current.id());
            if (dependanceOpt.isEmpty()) {
                continue;
            }
            for (UUID cibleId : dependanceOpt.get().cibleTacheIds()) {
                if (!visites.add(cibleId)) {
                    continue;
                }
                tacheSpi.findById(cibleId).ifPresent(tacheDependante -> {
                    resultat.add(tacheDependante);
                    pile.add(tacheDependante);
                });
            }
        }

        return resultat;
    }
    @Override
    public TacheDTO annulerValidationTache(UUID tacheId) {
        InstanceTache tache = tacheSpi.findById(tacheId)
            .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'ID: " + tacheId));
        
        if (tache.groupeTacheId() == null) {
            throw new RuntimeException("Cette tâche n'appartient pas à un groupe");
        }
        
        // Récupérer le groupe
        InstanceGroupeTache groupe = groupeTacheSpi.findById(tache.groupeTacheId())
            .orElseThrow(() -> new RuntimeException("Groupe de tâches non trouvé"));
        
        // Trouver toutes les tâches du processus qui dépendent de cette tâche
        List<InstanceTache> tachesDependantes = trouverTachesDependante(tache);
        
        // Remettre la tâche source à "en cours" si elle n'est pas déjà à en cours"
        if (!tache.statut().equals(StatutTache.enCours)) {
            InstanceTache tacheRemise = new InstanceTache(
                tache.id(),
                tache.code(),
                tache.libelle(),
                tache.contenu(),
                StatutTache.enCours,
                tache.dateEcheance(),
                tache.templateId(),
                tache.groupeTacheId(),
                tache.dependanceId()
            );
            tacheSpi.save(tacheRemise);
            logger.info("Tâche source {} remise à 'en cours'", tache.id());
        }
        
        HashSet<UUID> groupesImpactes = new HashSet<>();
        // Remettre toutes les tâches dépendantes à "à faire"
        for (InstanceTache tacheDependante : tachesDependantes) {
            if (!tacheDependante.statut().equals(StatutTache.aFaire)) {
                InstanceTache tacheRemise = new InstanceTache(
                    tacheDependante.id(),
                    tacheDependante.code(),
                    tacheDependante.libelle(),
                    tacheDependante.contenu(),
                    StatutTache.aFaire,
                    tacheDependante.dateEcheance(),
                    tacheDependante.templateId(),
                    tacheDependante.groupeTacheId(),
                    tacheDependante.dependanceId()
                );
                tacheSpi.save(tacheRemise);
                logger.info("Tâche dépendante {} remise à 'à faire'", tacheDependante.id());
                if (tacheDependante.groupeTacheId() != null) {
                    groupesImpactes.add(tacheDependante.groupeTacheId());
                }
            }
        }
        
        groupesImpactes.add(groupe.id());
        for (UUID groupeId : groupesImpactes) {
            InstanceGroupeTache groupeRecharge = groupeTacheSpi.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe de tâches non trouvé après mise à jour des tâches"));
            InstanceGroupeTache groupeMisAJour = groupeRecharge.changerStatut();
            groupeTacheSpi.save(groupeMisAJour);
            logger.info("Groupe {} mis à jour au statut {}", groupeId, groupeMisAJour.statut());
        }
        
        // Retourner la tâche mise à jour (qui est maintenant à "en cours")
        InstanceTache tacheMiseAJour = tacheSpi.findById(tacheId)
            .orElseThrow(() -> new RuntimeException("Tâche non trouvée après mise à jour"));
        
        return converter.convertirEnDTO(tacheMiseAJour);
    }
}
