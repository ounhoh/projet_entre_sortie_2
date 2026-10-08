package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessDetailDTO;
import com.cese.process_entree_sortie.application.port.in.processus.GetProcessusDetailsApi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetProcessusDetailsService implements GetProcessusDetailsApi {
    
    private final ProcessusSpi processusSpi;
    private final ProcessusConverter processusConverter;
    
    public GetProcessusDetailsService(ProcessusSpi processusSpi, ProcessusConverter processusConverter) {
        this.processusSpi = processusSpi;
        this.processusConverter = processusConverter;
    }
    
    @Override
    public ProcessDetailDTO getProcessusDetail(UUID processId) {
        InstanceProcessus processus = processusSpi.findById(processId)
            .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'ID: " + processId));
        
        return processusConverter.convertirEnDetailDTO(processus);
    }
}