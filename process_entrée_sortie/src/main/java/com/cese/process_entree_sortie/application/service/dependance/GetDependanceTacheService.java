package com.cese.process_entree_sortie.application.service.dependance;

import com.cese.process_entree_sortie.application.dto.tache.sortie.DependanceDTO;
import com.cese.process_entree_sortie.application.port.in.dependance.GetDependanceTacheApi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetDependanceTacheService implements GetDependanceTacheApi {
    
    private final DependanceSpi dependanceSpi;
    private final DependanceConverter converter;
    
    public GetDependanceTacheService(DependanceSpi dependanceSpi, DependanceConverter converter) {
        this.dependanceSpi = dependanceSpi;
        this.converter = converter;
    }
    
    @Override
    public DependanceDTO getDependanceTache(UUID tacheId) {
        // Récupérer la première dépendance liée à cette tâche
        List<InstanceDependance> dependances = dependanceSpi.findByTacheId(tacheId);
        if (dependances.isEmpty()) {
            throw new RuntimeException("Aucune dépendance trouvée pour la tâche avec l'ID: " + tacheId);
        }
        // Retourner la première dépendance trouvée
        return converter.convertirEnDTO(dependances.get(0));
    }
}
