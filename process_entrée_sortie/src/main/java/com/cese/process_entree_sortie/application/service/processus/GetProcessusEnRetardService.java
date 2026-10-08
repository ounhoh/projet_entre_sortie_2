package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.processus.GetProcessusEnRetardApi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetProcessusEnRetardService implements GetProcessusEnRetardApi {
    
    private final ProcessusSpi processusSpi;
    private final ProcessusConverter processusConverter;
    
    public GetProcessusEnRetardService(ProcessusSpi processusSpi, ProcessusConverter processusConverter) {
        this.processusSpi = processusSpi;
        this.processusConverter = processusConverter;
    }
    
    @Override
    public List<ProcessusDTO> getProcessusEnRetard() {
        List<InstanceProcessus> processus = processusSpi.findEnRetards();
        return processus.stream()
            .map(processusConverter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}