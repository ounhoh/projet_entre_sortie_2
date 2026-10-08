package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.StatutProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.processus.GetProcessusByIdApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentDirectionSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDirection;
import com.cese.process_entree_sortie.domain.Direction.model.Direction;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.application.service.agent.AgentConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetProcessusByIdService implements GetProcessusByIdApi {
    
    private final ProcessusSpi processusSpi;
    private final DirectionSpi directionSpi;
    private final AgentSpi agentSpi;
    private final AgentDirectionSpi agentDirectionSpi;
    
    public GetProcessusByIdService(ProcessusSpi processusSpi, DirectionSpi directionSpi, AgentSpi agentSpi, AgentDirectionSpi agentDirectionSpi) {
        this.processusSpi = processusSpi;
        this.directionSpi = directionSpi;
        this.agentSpi = agentSpi;
        this.agentDirectionSpi = agentDirectionSpi;
    }
    
    @Override
    public ProcessusDTO getProcessById(UUID processusId) {
        InstanceProcessus processus = processusSpi.findById(processusId)
            .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'ID: " + processusId));
        
        return convertirEnDTO(processus);
    }
    
    private ProcessusDTO convertirEnDTO(InstanceProcessus processus) {
        // Charger les dépendances nécessaires
        Direction direction = directionSpi.findById(processus.directionConcerneeId())
            .orElseThrow(() -> new RuntimeException("Direction non trouvée"));
        
        AgentPersonnel agent = agentSpi.findById(processus.agentId())
            .orElseThrow(() -> new RuntimeException("Agent non trouvé"));
        
        // Créer StatutProcessusDTO - pour l'instant avec des valeurs par défaut, à améliorer avec un SPI dédié
        StatutProcessusDTO statutDTO = new StatutProcessusDTO(processus.statutId(), "", "");
        
        // Créer AgentDTO en utilisant AgentConverter pour inclure la date de sortie et le libellé de direction
        com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO agentDTO = 
            AgentConverter.convertirEnDTO(agent, directionSpi);
        
        LocalDate dateMobilite = null;
        Optional<AgentDirection> agentDirectionOpt = agentDirectionSpi.findCurrentByAgentId(processus.agentId());
        if (agentDirectionOpt.isPresent()) {
            AgentDirection agentDirection = agentDirectionOpt.get();
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
            direction.libDirection(),
            agentDTO,
            processus.typeProcessus(),
            dateMobilite,
            LocalDateTime.now(), // dateArrivee - à récupérer depuis le processus si disponible
            processus.dateEcheance(),
            processus.templateId()
        );
    }
}