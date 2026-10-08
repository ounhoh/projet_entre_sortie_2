package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Direction.model.Direction;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.DirectionEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.DirectionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class DirectionAdapter implements DirectionSpi {
    
    private final DirectionJpaRepository jpaRepository;
    
    public DirectionAdapter(DirectionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Direction save(Direction direction) {
        DirectionEntity entity = convertirEnEntity(direction);
        DirectionEntity entitySauvegardee = jpaRepository.save(entity);
        return convertirEnDomain(entitySauvegardee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Direction> findById(UUID directionId) {
        return jpaRepository.findById(directionId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Direction> findAll() {
        return jpaRepository.findAll().stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(Direction direction) {
        DirectionEntity entity = convertirEnEntity(direction);
        jpaRepository.delete(entity);
    }
    
    private DirectionEntity convertirEnEntity(Direction direction) {
        return new DirectionEntity(
            direction.id(),
            direction.codeDirection(),
            direction.libDirection()
        );
    }
    
    private Direction convertirEnDomain(DirectionEntity entity) {
        return new Direction(
            entity.getId(),
            entity.getCodeDirection(),
            entity.getLibDirection()
        );
    }
}