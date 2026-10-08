package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.entree.AssignerAgentToTacheCommand;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.AssignerAgentToTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssignerAgentToTacheService implements AssignerAgentToTacheApi {
    
    private final TacheSpi tacheSpi;
    private final GroupeTacheSpi groupeTacheSpi;
    private final TacheConverter converter;
    
    public AssignerAgentToTacheService(TacheSpi tacheSpi, GroupeTacheSpi groupeTacheSpi, TacheConverter converter) {
        this.tacheSpi = tacheSpi;
        this.groupeTacheSpi = groupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TacheDTO assignerAgentToTache(AssignerAgentToTacheCommand command) {
        InstanceTache tache = tacheSpi.findById(command.tacheId())
            .orElseThrow(() -> new RuntimeException("Tache non trouvée avec l'ID: " + command.tacheId()));
        
        if (tache.groupeTacheId() == null) {
            throw new RuntimeException("La tâche n'appartient à aucun groupe de tâches");
        }
        
        // Récupérer le groupe de tâches et assigner l'agent au groupe
        InstanceGroupeTache groupe = groupeTacheSpi.findById(tache.groupeTacheId())
            .orElseThrow(() -> new RuntimeException("Groupe de tâches non trouvé avec l'ID: " + tache.groupeTacheId()));
        
        InstanceGroupeTache groupeModifie = groupe.assignerAgent(command.agentId());
        groupeTacheSpi.save(groupeModifie);
        
        // Recharger la tâche pour s'assurer qu'on a la dernière version
        InstanceTache tacheSauvegarde = tacheSpi.findById(command.tacheId())
            .orElseThrow(() -> new RuntimeException("Tache non trouvée après modification du groupe"));
        
        return converter.convertirEnDTO(tacheSauvegarde);
    }
}
