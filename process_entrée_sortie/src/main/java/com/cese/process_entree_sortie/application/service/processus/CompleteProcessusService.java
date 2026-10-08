package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.processus.CompleteProcessusApi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CompleteProcessusService implements CompleteProcessusApi {
    
    private final ProcessusSpi processusSpi;
    private final ProcessusConverter processusConverter;
    
    public CompleteProcessusService(ProcessusSpi processusSpi, ProcessusConverter processusConverter) {
        this.processusSpi = processusSpi;
        this.processusConverter = processusConverter;
    }
    
    @Override
    public ProcessusDTO completeProcessus(UUID processusId) {
        InstanceProcessus processus = processusSpi.findById(processusId)
            .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'ID: " + processusId));
        
        // TODO: Déterminer le statut "complet" - pour l'instant on garde le même statut
        // InstanceProcessus processusModifie = processus.changerStatut(statutCompletId);
        InstanceProcessus processusSauvegarde = processusSpi.save(processus);
        
        return processusConverter.convertirEnDTO(processusSauvegarde);
    }
}