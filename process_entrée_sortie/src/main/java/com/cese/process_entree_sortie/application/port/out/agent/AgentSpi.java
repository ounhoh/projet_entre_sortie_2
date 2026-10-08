package com.cese.process_entree_sortie.application.port.out.agent;

import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface AgentSpi {
    // sauvergarder un agent
    AgentPersonnel save(AgentPersonnel agent);
    Optional<AgentPersonnel> findById(UUID agentId);
    Optional<AgentPersonnel> findByEmail(String email);
    Optional<AgentPersonnel> findByCodeAgent(String code);
    boolean exisByCodeAgent(String code);
    List<AgentPersonnel> findAll(int page, int size, String sort,String direction);
    List<AgentPersonnel> search(String term, String role,UUID directionId);
    void delete(AgentPersonnel agent);
    void deleteById(UUID agentId);

}
