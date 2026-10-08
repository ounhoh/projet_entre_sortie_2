package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.port.in.agent.DeleteAgentApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentDiffusionSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentDirectionSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentMaterielSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeleteAgentService implements DeleteAgentApi {
    
    private final AgentSpi agentSpi;
    private final ProcessusSpi processusSpi;
    private final GroupeTacheSpi groupeTacheSpi;
    private final TacheSpi tacheSpi;
    private final DependanceSpi dependanceSpi;
    private final NotificationSpi notificationSpi;
    private final AgentDirectionSpi agentDirectionSpi;
    private final AgentMaterielSpi agentMaterielSpi;
    private final AgentDiffusionSpi agentDiffusionSpi;
    private final AffectationSpi affectationSpi;
    
    public DeleteAgentService(
            AgentSpi agentSpi,
            ProcessusSpi processusSpi,
            GroupeTacheSpi groupeTacheSpi,
            TacheSpi tacheSpi,
            DependanceSpi dependanceSpi,
            NotificationSpi notificationSpi,
            AgentDirectionSpi agentDirectionSpi,
            AgentMaterielSpi agentMaterielSpi,
            AgentDiffusionSpi agentDiffusionSpi,
            AffectationSpi affectationSpi) {
        this.agentSpi = agentSpi;
        this.processusSpi = processusSpi;
        this.groupeTacheSpi = groupeTacheSpi;
        this.tacheSpi = tacheSpi;
        this.dependanceSpi = dependanceSpi;
        this.notificationSpi = notificationSpi;
        this.agentDirectionSpi = agentDirectionSpi;
        this.agentMaterielSpi = agentMaterielSpi;
        this.agentDiffusionSpi = agentDiffusionSpi;
        this.affectationSpi = affectationSpi;
    }
    
    @Override
    public void deleteAgent(UUID agentId) {
        // Supprimer les notifications où l'agent est destinataire
        notificationSpi.findByAgentId(agentId)
            .forEach(notificationSpi::delete);
        
        // Supprimer les processus liés à l'agent (et leurs dépendances)
        List<InstanceProcessus> processusList = processusSpi.findByAgentId(agentId);
        for (InstanceProcessus processus : processusList) {
            supprimerProcessusEtDependances(processus);
        }
        
        // Supprimer les affectations, directions, diffusion et matériel
        affectationSpi.findByAgentId(agentId).forEach(affectationSpi::delete);
        agentDirectionSpi.findByAgentId(agentId).forEach(agentDirectionSpi::delete);
        agentDiffusionSpi.deleteByAgentId(agentId);
        agentMaterielSpi.deleteByAgentId(agentId);
        
        // Supprimer l'agent
        agentSpi.deleteById(agentId);
    }

    private void supprimerProcessusEtDependances(InstanceProcessus processus) {
        List<InstanceGroupeTache> groupes = groupeTacheSpi.findByProcessusId(processus.id());
        List<InstanceTache> taches = tacheSpi.findbyProcessusId(processus.id());
        
        List<UUID> groupeIds = new ArrayList<>();
        for (InstanceGroupeTache groupe : groupes) {
            groupeIds.add(groupe.id());
        }
        
        List<UUID> tacheIds = new ArrayList<>();
        for (InstanceTache tache : taches) {
            tacheIds.add(tache.id());
        }
        
        // Supprimer les notifications liées aux tâches/groupes du processus
        notificationSpi.findByGroupeTacheIdsOrTacheIds(groupeIds, tacheIds)
            .forEach(notificationSpi::delete);
        
        // Supprimer les dépendances
        dependanceSpi.findByProcessusId(processus.id())
            .forEach(dependanceSpi::delete);
        
        // Supprimer les tâches
        for (InstanceTache tache : taches) {
            tacheSpi.delete(tache);
        }
        
        // Supprimer les groupes
        for (InstanceGroupeTache groupe : groupes) {
            groupeTacheSpi.delete(groupe);
        }
        
        // Supprimer le processus
        processusSpi.delete(processus);
    }
}