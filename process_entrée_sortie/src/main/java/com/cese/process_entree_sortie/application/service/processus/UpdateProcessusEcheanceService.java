package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.dto.processus.entree.UpdateEcheanceCommand;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.processus.UpdateProcessusEcheanceApi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateProcessusEcheanceService implements UpdateProcessusEcheanceApi {
    
    private final ProcessusSpi processusSpi;
    private final ProcessusConverter processusConverter;
    
    public UpdateProcessusEcheanceService(ProcessusSpi processusSpi, ProcessusConverter processusConverter) {
        this.processusSpi = processusSpi;
        this.processusConverter = processusConverter;
    }
    
    @Override
    public ProcessusDTO updateProcessusEcheance(UpdateEcheanceCommand command) {
        InstanceProcessus processus = processusSpi.findById(command.processId())
            .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'ID: " + command.processId()));
        
        // Créer un nouveau processus avec la nouvelle date d'échéance
        InstanceProcessus processusModifie = new InstanceProcessus(
            processus.id(),
            processus.codeProcessus(),
            processus.dateCreation(),
            command.nouvelleEcheance(),
            processus.directionConcerneeId(),
            processus.agentId(),
            processus.templateId(),
            processus.statutId(),
            processus.groupeTachesList(),
            processus.dependances(),
            processus.typeProcessus()
        );
        
        InstanceProcessus processusSauvegarde = processusSpi.save(processusModifie);
        return processusConverter.convertirEnDTO(processusSauvegarde);
    }
}