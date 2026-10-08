package com.cese.process_entree_sortie.domain.Agent.service;

import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;

import java.util.UUID;
import java.util.List;

public  class AgentService {

    // Vérification de l'existence d'un agent
    public Boolean agentExistantId(UUID agentId, List<AgentPersonnel> agentPersonnelList)
    {
        return agentPersonnelList.stream().anyMatch(agent -> agent.id().equals(agentId));
    }

    // Vérification qu'un agent est bien dans qu'un seul process entrée
    public Boolean agentDansUnSeulProcessActif(UUID agentId, List<InstanceProcessus> processusList)
    {
        return processusList.stream()
                .filter(process -> process.typeProcessus().equals(TypeProcessus.entree))
                .filter(process -> process.agentId().equals(agentId))
                .toArray().length == 1;
    }

    // Vérification qu'un agent est bien dans qu'un seul process sortie
    public Boolean agentDansUnSeulProcessSortie(UUID agentId, List<InstanceProcessus> processusList)
    {
        return processusList.stream()
                .filter(process -> process.typeProcessus().equals(TypeProcessus.sortie))
                .filter(process -> process.agentId().equals(agentId))
                .toArray().length == 1;
    }

}
