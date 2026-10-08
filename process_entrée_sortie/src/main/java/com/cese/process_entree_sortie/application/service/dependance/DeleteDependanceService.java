package com.cese.process_entree_sortie.application.service.dependance;

import com.cese.process_entree_sortie.application.port.in.dependance.DeleteDependanceApi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteDependanceService implements DeleteDependanceApi {
    
    private final DependanceSpi dependanceSpi;
    
    public DeleteDependanceService(DependanceSpi dependanceSpi) {
        this.dependanceSpi = dependanceSpi;
    }
    
    @Override
    public void deleteDependance(UUID dependanceId) {
        InstanceDependance dependance = dependanceSpi.findById(dependanceId)
            .orElseThrow(() -> new RuntimeException("Dependance non trouvée avec l'ID: " + dependanceId));
        dependanceSpi.delete(dependance);
    }
}
