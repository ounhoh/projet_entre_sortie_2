package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceDependanceCibleEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceDependanceEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.InstanceDependanceJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class DependanceAdapter implements DependanceSpi {
    
    private final InstanceDependanceJpaRepository jpaRepository;
    
    public DependanceAdapter(InstanceDependanceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public InstanceDependance save(InstanceDependance dependance) {
        InstanceDependanceEntity entity = convertirEnEntity(dependance);
        InstanceDependanceEntity entitySauvegardee = jpaRepository.save(entity);
        return convertirEnDomain(entitySauvegardee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstanceDependance> findById(UUID dependanceId) {
        return jpaRepository.findById(dependanceId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceDependance> findByProcessusId(UUID processusId) {
        return jpaRepository.findByProcessusId(processusId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstanceDependance> findBySourceTacheId(UUID sourceTacheId) {
        return jpaRepository.findBySourceTacheId(sourceTacheId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceDependance> findByTacheId(UUID tacheId) {
        return jpaRepository.findByTacheId(tacheId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(InstanceDependance dependance) {
        jpaRepository.deleteById(dependance.id());
    }
    
    private InstanceDependanceEntity convertirEnEntity(InstanceDependance dependance) {
        InstanceDependanceEntity entity = new InstanceDependanceEntity(
            dependance.id(),
            dependance.sourceTacheId(),
            dependance.templateId()
        );
        
        // Créer les entités cibles
        for (UUID cibleTacheId : dependance.cibleTacheIds()) {
            InstanceDependanceCibleEntity cible = new InstanceDependanceCibleEntity(entity, cibleTacheId);
            entity.getCibles().add(cible);
        }
        
        return entity;
    }
    
    private InstanceDependance convertirEnDomain(InstanceDependanceEntity entity) {
        List<UUID> cibleTacheIds = entity.getCibles().stream()
            .map(InstanceDependanceCibleEntity::getCibleTacheId)
            .collect(Collectors.toList());
        
        return new InstanceDependance(
            entity.getId(),
            entity.getSourceTacheId(),
            cibleTacheIds,
            entity.getTemplateId()
        );
    }
}