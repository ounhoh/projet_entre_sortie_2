package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.ListGroupesByProcessusApi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListGroupesByProcessusService implements ListGroupesByProcessusApi {
    
    private final GroupeTacheSpi groupeTacheSpi;
    private final GroupeTacheConverter converter;
    
    public ListGroupesByProcessusService(GroupeTacheSpi groupeTacheSpi, GroupeTacheConverter converter) {
        this.groupeTacheSpi = groupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public List<GroupeTacheDTO> getListGroupesByProcessus(UUID processusId) {
        List<InstanceGroupeTache> groupes = groupeTacheSpi.findByProcessusId(processusId);
        return groupes.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
