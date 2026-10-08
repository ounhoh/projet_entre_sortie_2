package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.StartGroupeTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class StartGroupeTacheService implements StartGroupeTacheApi {
    
    private final GroupeTacheSpi groupeTacheSpi;
    private final GroupeTacheConverter converter;
    
    public StartGroupeTacheService(GroupeTacheSpi groupeTacheSpi, GroupeTacheConverter converter) {
        this.groupeTacheSpi = groupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public GroupeTacheDTO startGroupeTache(UUID groupeId) {
        InstanceGroupeTache groupe = groupeTacheSpi.findById(groupeId)
            .orElseThrow(() -> new RuntimeException("Groupe tache non trouvé avec l'ID: " + groupeId));
        
        InstanceGroupeTache groupeDemarre = groupe.changerStatut();
        InstanceGroupeTache groupeSauvegarde = groupeTacheSpi.save(groupeDemarre);
        return converter.convertirEnDTO(groupeSauvegarde);
    }
}
