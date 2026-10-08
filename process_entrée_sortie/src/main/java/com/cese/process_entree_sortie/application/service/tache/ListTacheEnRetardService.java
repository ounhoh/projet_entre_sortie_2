package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.ListTacheEnRetardApi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;
@Service
@Transactional(readOnly = true)
public class ListTacheEnRetardService implements ListTacheEnRetardApi {
    
    private final TacheSpi tacheSpi;
    private final TacheConverter converter;
    
    public ListTacheEnRetardService(TacheSpi tacheSpi, TacheConverter converter) {
        this.tacheSpi = tacheSpi;
        this.converter = converter;
    }
    
    @Override
    public List<TacheDTO> getListTacheEnRetard(UUID agentId) {
        List<InstanceTache> tachesEnRetard = tacheSpi.findEnRetard();
        if (tachesEnRetard.isEmpty()) {
            return new ArrayList<>();
        }
        return tachesEnRetard.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
