package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.GetTacheByIdApi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetTacheByIdService implements GetTacheByIdApi {
    
    private final TacheSpi tacheSpi;
    private final TacheConverter converter;
    
    public GetTacheByIdService(TacheSpi tacheSpi, TacheConverter converter) {
        this.tacheSpi = tacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TacheDTO getTacheById(UUID tacheId) {
        InstanceTache tache = tacheSpi.findById(tacheId)
            .orElseThrow(() -> new RuntimeException("Tache non trouvée avec l'ID: " + tacheId));
        return converter.convertirEnDTO(tache);
    }
}
