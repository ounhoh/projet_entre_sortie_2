package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessDetailDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.StatutProcessusDTO;
import com.cese.process_entree_sortie.application.dto.tache.sortie.DependanceDTO;
import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentDirectionSpi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDirection;
import com.cese.process_entree_sortie.domain.Direction.model.Direction;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.application.service.agent.AgentConverter;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.service.tache.GroupeTacheConverter;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.service.dependance.DependanceConverter;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.StatutProcessusJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessusConverter {
    
    private final DirectionSpi directionSpi;
    private final AgentSpi agentSpi;
    private final AgentDirectionSpi agentDirectionSpi;
    private final StatutProcessusJpaRepository statutProcessusJpaRepository;
    private final GroupeTacheSpi groupeTacheSpi;
    private final GroupeTacheConverter groupeTacheConverter;
    private final DependanceSpi dependanceSpi;
    private final DependanceConverter dependanceConverter;

    public ProcessusConverter(DirectionSpi directionSpi, AgentSpi agentSpi, AgentDirectionSpi agentDirectionSpi,
                              StatutProcessusJpaRepository statutProcessusJpaRepository, GroupeTacheSpi groupeTacheSpi, GroupeTacheConverter groupeTacheConverter, DependanceSpi dependanceSpi, DependanceConverter dependanceConverter) {
        this.directionSpi = directionSpi;
        this.agentSpi = agentSpi;
        this.agentDirectionSpi = agentDirectionSpi;
        this.statutProcessusJpaRepository = statutProcessusJpaRepository;
        this.groupeTacheSpi = groupeTacheSpi;
        this.groupeTacheConverter = groupeTacheConverter;
        this.dependanceSpi = dependanceSpi;
        this.dependanceConverter = dependanceConverter;
    }
    
    public ProcessusDTO convertirEnDTO(InstanceProcessus processus) {
        // Charger les dépendances nécessaires
        Direction direction = null;
        if (processus.directionConcerneeId() != null) {
            direction = directionSpi.findById(processus.directionConcerneeId()).orElse(null);
            if (direction == null) {
                System.out.println("Direction non trouvée pour le processus " + processus.id() +
                    " (directionConcerneeId=" + processus.directionConcerneeId() + ")");
            }
        }
        
        AgentPersonnel agent = agentSpi.findById(processus.agentId())
            .orElseThrow(() -> new RuntimeException("Agent non trouvé"));
        
        // Créer StatutProcessusDTO - récupérer le code/libellé réels
        StatutProcessusDTO statutDTO = statutProcessusJpaRepository.findById(processus.statutId())
            .map(sp -> new StatutProcessusDTO(sp.getId(), sp.getCodeStatut(), sp.getLibStatut()))
            .orElseGet(() -> new StatutProcessusDTO(processus.statutId(), "", ""));
        
        // Créer AgentDTO en utilisant AgentConverter pour inclure la date de sortie et le libellé de direction
        AgentDTO agentDTO = AgentConverter.convertirEnDTO(agent, directionSpi);
        
        java.time.LocalDate dateMobilite = null;
        AgentDirection agentDirection = agentDirectionSpi.findCurrentByAgentId(processus.agentId()).orElse(null);
        if (agentDirection != null) {
            switch (processus.typeProcessus()) {
                case sortie:
                    dateMobilite = agentDirection.dateDepart();
                    break;
                case entree:
                    dateMobilite = agentDirection.dateArrivee();
                    break;
                case mobitliteInterne:
                    dateMobilite = agentDirection.dateArrivee();
                    break;
                default:
                    throw new RuntimeException("Type de processus non valide: " + processus.typeProcessus());
            }
        }
        
        return new ProcessusDTO(
            processus.id(),
            processus.codeProcessus(),
            statutDTO,
            direction != null ? direction.libDirection() : "Direction supprimée",
            agentDTO,
            processus.typeProcessus(),
            dateMobilite,
            processus.dateCreation(), // dateArrivee
            processus.dateEcheance(),
            processus.templateId()
        );
    }
    
    public ProcessDetailDTO convertirEnDetailDTO(InstanceProcessus processus) {
        ProcessusDTO processusDTO = convertirEnDTO(processus);
        List<InstanceGroupeTache> groupes = groupeTacheSpi.findByProcessusId(processus.id());
        List<GroupeTacheDTO> groupeTachesList = groupes.stream()
            .map(groupeTacheConverter::convertirEnDTO)
            .collect(Collectors.toList());
        List<DependanceDTO> dependancesList;
        List<InstanceDependance> dependances = dependanceSpi.findByProcessusId(processus.id());
        dependancesList = dependances.stream()
            .map(dependanceConverter::convertirEnDTO)
            .collect(Collectors.toList());
        return new ProcessDetailDTO(
            processusDTO,
            groupeTachesList,
            dependancesList
        );
    }
}