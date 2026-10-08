package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.GetGroupeBloqueeApi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetGroupeBloqueeService implements GetGroupeBloqueeApi {
    
    private final GroupeTacheSpi groupeTacheSpi;
    private final DependanceSpi dependanceSpi;
    private final TacheSpi tacheSpi;
    private final GroupeTacheConverter converter;
    
    public GetGroupeBloqueeService(GroupeTacheSpi groupeTacheSpi, DependanceSpi dependanceSpi, TacheSpi tacheSpi, GroupeTacheConverter converter) {
        this.groupeTacheSpi = groupeTacheSpi;
        this.dependanceSpi = dependanceSpi;
        this.tacheSpi = tacheSpi;
        this.converter = converter;
    }
    
    @Override
    public List<GroupeTacheDTO> getGroupeBloquee(UUID processusId) {
        List<InstanceGroupeTache> tousGroupes = groupeTacheSpi.findByProcessusId(processusId);
        List<InstanceDependance> dependances = dependanceSpi.findByProcessusId(processusId);
        
        // Filtrer les groupes qui ne sont pas prêts à démarrer (bloqués par des dépendances)
        return tousGroupes.stream()
            .filter(groupe -> !groupe.statut().equals(StatutTache.fait))
            .filter(groupe -> !estPretADemarrer(groupe, dependances, tousGroupes))
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Un groupe est prêt à démarrer si toutes ses tâches sont prêtes.
     * Une tâche est prête si elle n'a pas de dépendances entrantes (dependanceId == null)
     * ou si toutes ses dépendances sont satisfaites (la tâche source est terminée).
     */
    private boolean estPretADemarrer(InstanceGroupeTache groupe, List<InstanceDependance> dependances, List<InstanceGroupeTache> tousGroupes) {
        // Vérifier toutes les tâches du groupe
        for (InstanceTache tache : groupe.tacheList()) {
            if (!tacheEstPrete(tache)) {
                return false; // Si une tâche n'est pas prête, le groupe n'est pas prêt
            }
        }
        return true; // Toutes les tâches sont prêtes
    }
    
    /**
     * Vérifie si une tâche est prête à démarrer.
     * Une tâche est prête si elle n'a pas de dépendances entrantes ou si toutes ses dépendances sont satisfaites.
     */
    private boolean tacheEstPrete(InstanceTache tache) {
        // Si la tâche n'a pas de dépendance (dependanceId == null), elle est prête
        if (tache.dependanceId() == null) {
            return true;
        }
        
        // Récupérer la dépendance
        java.util.Optional<InstanceDependance> dependanceOpt = dependanceSpi.findById(tache.dependanceId());
        if (dependanceOpt.isEmpty()) {
            return true; // Pas de dépendance trouvée = prête
        }
        
        InstanceDependance dependance = dependanceOpt.get();
        
        // Vérifier que la tâche source est terminée
        java.util.Optional<InstanceTache> tacheSourceOpt = tacheSpi.findById(dependance.sourceTacheId());
        if (tacheSourceOpt.isEmpty()) {
            return false; // Source non trouvée = pas prête
        }
        
        InstanceTache tacheSource = tacheSourceOpt.get();
        return tacheSource.statut().equals(StatutTache.fait);
    }
}
