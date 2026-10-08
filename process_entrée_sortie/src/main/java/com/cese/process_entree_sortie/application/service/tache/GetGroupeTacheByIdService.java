package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.GetGroupeTacheByIdApi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetGroupeTacheByIdService implements GetGroupeTacheByIdApi {
    
    private final GroupeTacheSpi groupeTacheSpi;
    private final GroupeTacheConverter converter;
    
    public GetGroupeTacheByIdService(GroupeTacheSpi groupeTacheSpi, GroupeTacheConverter converter) {
        this.groupeTacheSpi = groupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public GroupeTacheDTO getGroupeTacheById(UUID groupeId) {
        InstanceGroupeTache groupe = groupeTacheSpi.findById(groupeId)
            .orElseThrow(() -> new RuntimeException("Groupe tache non trouvé avec l'ID: " + groupeId));
        return converter.convertirEnDTO(groupe);
    }
}
