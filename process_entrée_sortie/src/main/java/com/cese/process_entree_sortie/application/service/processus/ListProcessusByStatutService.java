package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.StatutProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.processus.ListProcessusByStatutApi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListProcessusByStatutService implements ListProcessusByStatutApi {
    
    private final ProcessusSpi processusSpi;
    private final ProcessusConverter processusConverter;
    
    public ListProcessusByStatutService(ProcessusSpi processusSpi, ProcessusConverter processusConverter) {
        this.processusSpi = processusSpi;
        this.processusConverter = processusConverter;
    }
    
    @Override
    public List<ProcessusDTO> getListProcessusByStatut(StatutProcessusDTO statutProcessusDTO) {
        // Convertir DTO en domain model
        StatutProcessus statut = new StatutProcessus(
            statutProcessusDTO.id(),
            statutProcessusDTO.code(),
            statutProcessusDTO.libelle()
        );
        
        List<InstanceProcessus> processus = processusSpi.findByStatut(statut);
        return processus.stream()
            .map(processusConverter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}