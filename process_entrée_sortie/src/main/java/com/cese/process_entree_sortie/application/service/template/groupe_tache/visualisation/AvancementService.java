package com.cese.process_entree_sortie.application.service.template.groupe_tache.visualisation;

import com.cese.process_entree_sortie.application.service.tache.TacheConverter;
import com.cese.process_entree_sortie.application.service.tache.GroupeTacheConverter;
import com.cese.process_entree_sortie.application.service.processus.ProcessusConverter;
import com.cese.process_entree_sortie.application.dto.visualisation.sortie.AvancementDTO;
import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.visualisation.AvancementApi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@Transactional(readOnly = true)
public class AvancementService implements AvancementApi {
    
    private final TacheSpi tacheSpi;
    private final GroupeTacheSpi groupeTacheSpi;
    private final ProcessusSpi processusSpi;
    private final TacheConverter tacheConverter;
    private final GroupeTacheConverter groupeTacheConverter;
    private final ProcessusConverter processusConverter;
    
    public AvancementService(TacheSpi tacheSpi, GroupeTacheSpi groupeTacheSpi, ProcessusSpi processusSpi, TacheConverter tacheConverter, GroupeTacheConverter groupeTacheConverter, ProcessusConverter processusConverter) {
        this.tacheSpi = tacheSpi;
        this.groupeTacheSpi = groupeTacheSpi;
        this.processusSpi = processusSpi;
        this.tacheConverter = tacheConverter;
        this.groupeTacheConverter = groupeTacheConverter;
        this.processusConverter = processusConverter;
    }
    @Override
    public AvancementDTO getAvancementProcessus(UUID processusId) {
        InstanceProcessus processus = processusSpi.findById(processusId)
            .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'ID: " + processusId));
        List<InstanceGroupeTache> groupes = groupeTacheSpi.findByProcessusId(processusId);
        List<GroupeTacheDTO> groupesDTO = groupes.stream()
            .map(groupeTacheConverter::convertirEnDTO)
            .collect(Collectors.toList());
        return new AvancementDTO(processusConverter.convertirEnDetailDTO(processus), groupesDTO, new ArrayList<>());

    }
    
    @Override
    public AvancementDTO getAvancementGroupe(UUID groupeId) {
        InstanceGroupeTache groupe = groupeTacheSpi.findById(groupeId)
            .orElseThrow(() -> new RuntimeException("Groupe non trouvé avec l'ID: " + groupeId));
        List<InstanceTache> taches = tacheSpi.findByGroupeId(groupeId);
        List<TacheDTO> tachesDTO = taches.stream()
            .map(tacheConverter::convertirEnDTO)
            .collect(Collectors.toList());
        return new AvancementDTO(null, List.of(groupeTacheConverter.convertirEnDTO(groupe)), tachesDTO);
    }
    
}
