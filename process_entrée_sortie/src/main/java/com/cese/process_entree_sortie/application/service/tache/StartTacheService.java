package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.StartTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class StartTacheService implements StartTacheApi {
    
    private final TacheSpi tacheSpi;
    private final TacheConverter converter;
    
    public StartTacheService(TacheSpi tacheSpi, TacheConverter converter) {
        this.tacheSpi = tacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TacheDTO startTache(UUID tacheId) {
        InstanceTache tache = tacheSpi.findById(tacheId)
            .orElseThrow(() -> new RuntimeException("Tache non trouvée avec l'ID: " + tacheId));
        
        InstanceTache tacheDemarree = tache.demarer();
        InstanceTache tacheSauvegarde = tacheSpi.save(tacheDemarree);
        return converter.convertirEnDTO(tacheSauvegarde);
    }
}
