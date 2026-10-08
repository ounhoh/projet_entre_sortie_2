package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.DirectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DirectionJpaRepository extends JpaRepository<DirectionEntity, UUID> {
    java.util.Optional<DirectionEntity> findByCodeDirection(String codeDirection);
}