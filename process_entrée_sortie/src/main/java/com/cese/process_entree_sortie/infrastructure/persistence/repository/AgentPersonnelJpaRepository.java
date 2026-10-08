package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentPersonnelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentPersonnelJpaRepository extends JpaRepository<AgentPersonnelEntity, UUID> {
    Optional<AgentPersonnelEntity> findByEmail(String email);
    
    @Query("SELECT a FROM AgentPersonnelEntity a WHERE " +
           "(:term IS NULL OR LOWER(a.nom) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(a.prenom) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :term, '%'))) " +
           "AND (:role IS NULL OR a.role.code = :role)")
    Page<AgentPersonnelEntity> search(@Param("term") String term, 
                                      @Param("role") String role, 
                                      Pageable pageable);
}