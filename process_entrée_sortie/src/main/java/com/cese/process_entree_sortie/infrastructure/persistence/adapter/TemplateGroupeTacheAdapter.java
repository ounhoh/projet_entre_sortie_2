package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateGroupeTacheEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.TemplateGroupeTacheJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class TemplateGroupeTacheAdapter implements TemplateGroupeTacheSpi {
    
    private final TemplateGroupeTacheJpaRepository jpaRepository;
    
    public TemplateGroupeTacheAdapter(TemplateGroupeTacheJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public TemplateGroupeTache save(TemplateGroupeTache groupeTache) {
        TemplateGroupeTacheEntity entity = convertirEnEntity(groupeTache);
        TemplateGroupeTacheEntity entitySauvegardee = jpaRepository.save(entity);
        return convertirEnDomain(entitySauvegardee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TemplateGroupeTache> findById(UUID groupeId) {
        return jpaRepository.findById(groupeId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateGroupeTache> findByTemplateProcessusId(UUID processusId) {
        return jpaRepository.findByTemplateProcessusId(processusId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateGroupeTache> findByDirectionId(UUID directionId) {
        return jpaRepository.findByDirectionId(directionId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateGroupeTache> findAll() {
        return jpaRepository.findAll().stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(TemplateGroupeTache templateGroupeTache) {
        jpaRepository.deleteById(templateGroupeTache.id());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existById(UUID groupeId) {
        return jpaRepository.existsById(groupeId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByTemplateProcessusId(UUID processusId) {
        return jpaRepository.countByTemplateProcessusId(processusId);
    }
    
    private TemplateGroupeTacheEntity convertirEnEntity(TemplateGroupeTache groupe) {
        return new TemplateGroupeTacheEntity(
            groupe.id(),
            groupe.codeTemplate(),
            groupe.codeDirection(),
            groupe.libGroupTache(),
            groupe.statutProcessusId(),
            groupe.directionId(),
            groupe.templateProcessusId(),
            groupe.isDirectionConcernee()
        );
    }
    
    private TemplateGroupeTache convertirEnDomain(TemplateGroupeTacheEntity entity) {
        return new TemplateGroupeTache(
            entity.getId(),
            entity.getCodeTemplate(),
            entity.getCodeDirection(),
            entity.getLibGroupTache(),
            entity.getStatutProcessusId(),
            entity.getDirectionId(),
            entity.getTemplateProcessusId(),
            entity.isDirectionConcernee()
        );
    }
}