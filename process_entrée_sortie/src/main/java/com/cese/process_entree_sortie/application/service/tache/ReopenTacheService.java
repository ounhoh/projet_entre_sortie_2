package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.ReopenTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ReopenTacheService implements ReopenTacheApi {
    
    private final TacheSpi tacheSpi;
    private final TacheConverter converter;
    
    public ReopenTacheService(TacheSpi tacheSpi, TacheConverter converter) {
        this.tacheSpi = tacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TacheDTO reopenTache(UUID tacheId) {
        InstanceTache tacheInstance = tacheSpi.findById(tacheId)
            .orElseThrow(() -> new RuntimeException("Tache non trouvée avec l'ID: " + tacheId));
        
        InstanceTache tacheRouverte = tacheInstance.reouvertureTache();
        InstanceTache tacheSauvegarde = tacheSpi.save(tacheRouverte);
        return converter.convertirEnDTO(tacheSauvegarde);
    }
}
