package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.processus.entree.UpdateEcheanceCommand;
import com.cese.process_entree_sortie.application.dto.tache.entree.UpdateEcheanceTacheCommand;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.UpdateDateEchanceApi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateDateEcheanceService implements UpdateDateEchanceApi {
    
    private final TacheSpi tacheSpi;
    private final TacheConverter converter;
    
    public UpdateDateEcheanceService(TacheSpi tacheSpi, TacheConverter converter) {
        this.tacheSpi = tacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TacheDTO updateDateEcheance(UpdateEcheanceTacheCommand command) {
        InstanceTache tache = tacheSpi.findById(command.tacheId())
            .orElseThrow(() -> new RuntimeException("Tache non trouvée avec l'ID: " + command.tacheId()));

        InstanceTache tacheModifiee = tache.changerDateEcheance(command.nouvelleEcheance());
        InstanceTache tacheSauvegarde = tacheSpi.save(tacheModifiee);
        return converter.convertirEnDTO(tacheSauvegarde);
    }
}
