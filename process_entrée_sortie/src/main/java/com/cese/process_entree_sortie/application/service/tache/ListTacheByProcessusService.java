package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.ListTacheByProcessusApi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListTacheByProcessusService implements ListTacheByProcessusApi {
    
    private final TacheSpi tacheSpi;
    private final TacheConverter converter;
    
    public ListTacheByProcessusService(TacheSpi tacheSpi, TacheConverter converter) {
        this.tacheSpi = tacheSpi;
        this.converter = converter;
    }
    
    @Override
    public List<TacheDTO> getListTacheByProcessus(UUID processusId) {
        List<InstanceTache> taches = tacheSpi.findbyProcessusId(processusId);
        return taches.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
