package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentAffectationEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.AgentAffectationJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class AffectationAdapter implements AffectationSpi {
    
    private final AgentAffectationJpaRepository jpaRepository;
    
    public AffectationAdapter(AgentAffectationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public AgentAffectation save(AgentAffectation affectation) {
        AgentAffectationEntity entity = convertirEnEntity(affectation);
        AgentAffectationEntity entitySauvegardee = jpaRepository.save(entity);
        return convertirEnDomain(entitySauvegardee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AgentAffectation> findById(UUID affectationId) {
        return jpaRepository.findById(affectationId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AgentAffectation> findByAgentId(UUID agentId) {
        return jpaRepository.findByAgentId(agentId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AgentAffectation> fingActivesByAgentId(UUID agentId) {
        // Cette méthode n'est plus pertinente car l'état est maintenant dans AgentPersonnel
        // On retourne toutes les affectations de l'agent
        return jpaRepository.findByAgentId(agentId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AgentAffectation> findCurrentByAgentId(UUID agentId) {
        // Cette méthode n'est plus pertinente car l'état est maintenant dans AgentPersonnel
        // On retourne la première affectation de l'agent
        return jpaRepository.findByAgentId(agentId).stream()
            .findFirst()
            .map(this::convertirEnDomain);
    }
    
    @Override
    public void delete(AgentAffectation affectation) {
        // Pour supprimer, on doit d'abord trouver l'entité par les critères
        List<AgentAffectationEntity> entities = jpaRepository.findByAgentId(affectation.agentId());
        entities.stream()
            .filter(e -> e.getDirectionId().equals(affectation.directionId()))
            .forEach(jpaRepository::delete);
    }
    
    private AgentAffectationEntity convertirEnEntity(AgentAffectation affectation) {
        AgentAffectationEntity entity = new AgentAffectationEntity(
            affectation.Fonction(),
            affectation.agentResponsableId(),
            affectation.agentAcceuilId(),
            affectation.directionId(),
            affectation.agentId()
        );
        return entity;
    }
    
    private AgentAffectation convertirEnDomain(AgentAffectationEntity entity) {
        return new AgentAffectation(
            entity.getFonction(),
            entity.getAgentResponsableId(),
            entity.getAgentAcceuilId(),
            entity.getDirectionId(),
            entity.getAgentId()
        );
    }
}