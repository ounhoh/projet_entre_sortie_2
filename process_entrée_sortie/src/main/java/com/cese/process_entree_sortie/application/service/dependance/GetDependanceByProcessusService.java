package com.cese.process_entree_sortie.application.service.dependance;

import com.cese.process_entree_sortie.application.dto.tache.sortie.DependanceDTO;
import com.cese.process_entree_sortie.application.port.in.dependance.GetDependanceByProcessusApi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetDependanceByProcessusService implements GetDependanceByProcessusApi {
    
    private final DependanceSpi dependanceSpi;
    private final DependanceConverter converter;
    
    public GetDependanceByProcessusService(DependanceSpi dependanceSpi, DependanceConverter converter) {
        this.dependanceSpi = dependanceSpi;
        this.converter = converter;
    }
    
    @Override
    public List<DependanceDTO> getDependanceByProcessus(UUID processusId) {
        List<InstanceDependance> dependances = dependanceSpi.findByProcessusId(processusId);
        return dependances.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
