package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.agent.AgentDiffusionSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDiffusion;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentDiffusionEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.AgentDiffusionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class AgentDiffusionAdapter implements AgentDiffusionSpi {
    
    private final AgentDiffusionJpaRepository jpaRepository;
    
    public AgentDiffusionAdapter(AgentDiffusionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public AgentDiffusion save(AgentDiffusion diffusion) {
        // Vérifier si une liste existe déjà pour cet agent
        Optional<AgentDiffusionEntity> existing = jpaRepository.findByAgentId(diffusion.agentId());
        
        AgentDiffusionEntity entity;
        if (existing.isPresent()) {
            // Mettre à jour l'existant
            entity = existing.get();
            entity.setListeDiffusion(diffusion.listeDiffusion());
            entity.setSeparateur(diffusion.separateur());
        } else {
            // Créer un nouveau
            entity = new AgentDiffusionEntity(
                UUID.randomUUID(),
                diffusion.agentId(),
                diffusion.listeDiffusion(),
                diffusion.separateur()
            );
        }
        
        AgentDiffusionEntity saved = jpaRepository.save(entity);
        return convertirEnDomain(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AgentDiffusion> findByAgentId(UUID agentId) {
        return jpaRepository.findByAgentId(agentId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    public void deleteByAgentId(UUID agentId) {
        jpaRepository.findByAgentId(agentId)
            .ifPresent(jpaRepository::delete);
    }
    
    private AgentDiffusion convertirEnDomain(AgentDiffusionEntity entity) {
        return new AgentDiffusion(
            entity.getListeDiffusion(),
            entity.getSeparateur(),
            entity.getAgentId()
        );
    }
}
