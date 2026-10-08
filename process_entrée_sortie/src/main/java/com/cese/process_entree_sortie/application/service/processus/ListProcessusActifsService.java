package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.processus.ListProcessusActifsApi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListProcessusActifsService implements ListProcessusActifsApi {
    
    private static final Logger logger = LoggerFactory.getLogger(ListProcessusActifsService.class);
    
    private final ProcessusSpi processusSpi;
    private final ProcessusConverter processusConverter;
    
    public ListProcessusActifsService(ProcessusSpi processusSpi,
                                      ProcessusConverter processusConverter) {
        this.processusSpi = processusSpi;
        this.processusConverter = processusConverter;
    }
    
    @Override
    public List<ProcessusDTO> getListProcessusActifs() {
        List<InstanceProcessus> processus = processusSpi.findActifs();
        logger.debug("Nombre de processus actifs trouvés: {}", processus.size());
        return processus.stream()
            .map(processusConverter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}