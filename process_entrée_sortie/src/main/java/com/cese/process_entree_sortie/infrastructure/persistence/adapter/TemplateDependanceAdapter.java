package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.dependance.TemplateDependanceSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateDependance;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateDependanceEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.TemplateDependanceJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class TemplateDependanceAdapter implements TemplateDependanceSpi {
    
    private final TemplateDependanceJpaRepository jpaRepository;
    
    public TemplateDependanceAdapter(TemplateDependanceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public TemplateDependance save(TemplateDependance dependance) {
        TemplateDependanceEntity entity = convertirEnEntity(dependance);
        TemplateDependanceEntity entitySauvegardee = jpaRepository.save(entity);
        return convertirEnDomain(entitySauvegardee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TemplateDependance> findById(UUID dependanceId) {
        return jpaRepository.findById(dependanceId)
            .filter(entity -> {
                // S'assurer que les cibles sont chargées dans la transaction
                // et filtrer les dépendances sans cibles
                List<com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateDependanceCibleEntity> cibles = entity.getCibles();
                return cibles != null && !cibles.isEmpty();
            })
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateDependance> findByTemplateProcessusId(UUID processusId) {
        return jpaRepository.findByTemplateProcessusId(processusId).stream()
            .filter(entity -> {
                // S'assurer que les cibles sont chargées dans la transaction
                // et filtrer les dépendances sans cibles
                List<com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateDependanceCibleEntity> cibles = entity.getCibles();
                return cibles != null && !cibles.isEmpty();
            })
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(TemplateDependance dependance) {
        jpaRepository.deleteById(dependance.id());
    }
    
    private TemplateDependanceEntity convertirEnEntity(TemplateDependance dependance) {
        TemplateDependanceEntity entity = new TemplateDependanceEntity(
            dependance.id(),
            dependance.sourceTacheId()
        );
        
        // Créer les entités cibles
        for (UUID cibleTacheId : dependance.cibleTacheIds()) {
            com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateDependanceCibleEntity cible = 
                new com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateDependanceCibleEntity(entity, cibleTacheId);
            entity.getCibles().add(cible);
        }
        
        return entity;
    }
    
    private TemplateDependance convertirEnDomain(TemplateDependanceEntity entity) {
        // S'assurer que les cibles sont chargées (accès dans la transaction)
        List<com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateDependanceCibleEntity> cibles = entity.getCibles();
        
        // Si la collection est vide ou null, cela ne devrait pas arriver car on filtre avant
        // mais on ajoute une vérification de sécurité
        if (cibles == null || cibles.isEmpty()) {
            throw new IllegalStateException("La dépendance " + entity.getId() + " n'a pas de cibles chargées");
        }
        
        List<UUID> cibleTacheIds = cibles.stream()
            .map(com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateDependanceCibleEntity::getCibleTacheId)
            .collect(Collectors.toList());
        
        return new TemplateDependance(
            entity.getId(),
            entity.getSourceTacheId(),
            cibleTacheIds
        );
    }
}