package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.GetTacheBloqueApi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetTacheBloqueService implements GetTacheBloqueApi {
    
    private final TacheSpi tacheSpi;
    private final DependanceSpi dependanceSpi;
    private final TacheConverter converter;
    
    public GetTacheBloqueService(TacheSpi tacheSpi, DependanceSpi dependanceSpi, TacheConverter converter) {
        this.tacheSpi = tacheSpi;
        this.dependanceSpi = dependanceSpi;
        this.converter = converter;
    }
    
    @Override
    public List<TacheDTO> getTacheBloque(UUID processusId) {
        List<InstanceTache> toutesTaches = tacheSpi.findbyProcessusId(processusId);
        
        // Filtrer les tâches qui ne sont pas prêtes à démarrer (bloquées par des dépendances)
        return toutesTaches.stream()
            .filter(tache -> !tache.statut().equals(StatutTache.fait))
            .filter(tache -> !estPretADemarrer(tache, toutesTaches))
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
    
    private boolean estPretADemarrer(InstanceTache tache, List<InstanceTache> toutesTaches) {
        // Si la tâche n'a pas de dépendance (dependanceId == null), elle est prête
        if (tache.dependanceId() == null) {
            return true;
        }
        
        // Récupérer la dépendance
        java.util.Optional<InstanceDependance> dependanceOpt = dependanceSpi.findById(tache.dependanceId());
        if (dependanceOpt.isEmpty()) {
            return true; // Si la dépendance n'existe pas, considérer comme prête
        }
        
        InstanceDependance dependance = dependanceOpt.get();
        
        // Vérifier que la tâche source est terminée (statut = fait)
        InstanceTache sourceTache = toutesTaches.stream()
            .filter(t -> t.id().equals(dependance.sourceTacheId()))
            .findFirst()
            .orElse(null);
        
        return sourceTache != null && sourceTache.statut().equals(StatutTache.fait);
    }
}
