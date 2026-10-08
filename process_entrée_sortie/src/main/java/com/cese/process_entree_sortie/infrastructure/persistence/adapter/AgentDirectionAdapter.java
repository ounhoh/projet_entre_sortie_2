package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.agent.AgentDirectionSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDirection;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentDirectionEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.AgentDirectionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class AgentDirectionAdapter implements AgentDirectionSpi {
    
    private final AgentDirectionJpaRepository jpaRepository;
    
    public AgentDirectionAdapter(AgentDirectionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public AgentDirection save(AgentDirection direction) {
        AgentDirectionEntity entity = convertirEnEntity(direction);
        AgentDirectionEntity entitySauvegardee = jpaRepository.save(entity);
        return convertirEnDomain(entitySauvegardee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AgentDirection> findById(UUID agentDirectionId) {
        return jpaRepository.findById(agentDirectionId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AgentDirection> findByAgentId(UUID agentId) {
        return jpaRepository.findByAgentPersonnelId(agentId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AgentDirection> fingActivesByAgentId(UUID agentId) {
        return jpaRepository.findActivesByAgentId(agentId, LocalDate.now()).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AgentDirection> findCurrentByAgentId(UUID agentId) {
        return jpaRepository.findCurrentByAgentId(agentId, LocalDate.now()).stream()
            .findFirst()
            .map(this::convertirEnDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDirection> findByDirectionId(UUID directionId) {
        return jpaRepository.findByDirectionId(directionId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(AgentDirection agentDirection) {
        AgentDirectionEntity entity = convertirEnEntity(agentDirection);
        jpaRepository.delete(entity);
    }
    
    private AgentDirectionEntity convertirEnEntity(AgentDirection direction) {
        return new AgentDirectionEntity(
            direction.agentDirectionID(),
            direction.codeAgentDirection(),
            direction.codeAgent(),
            direction.codeDirection(),
            direction.directionId(),
            direction.dateArrivee(),
            direction.dateDepart(),
            direction.getOPtionalAgentId().orElse(null),
            direction.numeroBureau()
        );
    }
    
    private AgentDirection convertirEnDomain(AgentDirectionEntity entity) {
        return new AgentDirection(
            entity.getAgentDirectionID(),
            entity.getCodeAgentDirection(),
            entity.getCodeAgent(),
            entity.getCodeDirection(),
            entity.getDirectionId(),
            entity.getDateArrivee(),
            entity.getDateDepart(),
            entity.getAgentPersonnelId(),
            entity.getNumeroBureau()
        );
    }
}