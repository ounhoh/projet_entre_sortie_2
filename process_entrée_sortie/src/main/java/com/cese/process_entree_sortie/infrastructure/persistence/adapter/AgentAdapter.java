package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.agent.AgentDirectionSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDirection;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Agent.model.Role;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentPersonnelEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.RoleEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.AgentPersonnelJpaRepository;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.RoleJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class AgentAdapter implements AgentSpi {
    
    private final AgentPersonnelJpaRepository jpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final AgentDirectionSpi agentDirectionSpi;
    private final AffectationSpi affectationSpi;
    
    public AgentAdapter(AgentPersonnelJpaRepository jpaRepository,
                       RoleJpaRepository roleJpaRepository,
                       AgentDirectionSpi agentDirectionSpi,
                       AffectationSpi affectationSpi) {
        this.jpaRepository = jpaRepository;
        this.roleJpaRepository = roleJpaRepository;
        this.agentDirectionSpi = agentDirectionSpi;
        this.affectationSpi = affectationSpi;
    }
    
    @Override
    public AgentPersonnel save(AgentPersonnel agent) {
        AgentPersonnelEntity entity = convertirEnEntity(agent);
        AgentPersonnelEntity entitySauvegardee = jpaRepository.save(entity);
        
        // Sauvegarder les directions et affectations
        if (agent.agentDirections() != null) {
            agent.agentDirections().forEach(agentDirectionSpi::save);
        }
        if (agent.agentAffectations() != null) {
            agent.agentAffectations().forEach(affectationSpi::save);
        }
        
        return chargerComplet(entitySauvegardee.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AgentPersonnel> findById(UUID agentId) {
        return jpaRepository.findById(agentId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AgentPersonnel> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AgentPersonnel> findByCodeAgent(String code) {
        // Le codeAgent est dans AgentDirection, pas dans AgentPersonnel
        // Cette méthode devra être implémentée différemment selon la logique métier
        return Optional.empty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean exisByCodeAgent(String code) {
        return findByCodeAgent(code).isPresent();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AgentPersonnel> findAll(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return jpaRepository.findAll(pageable).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AgentPersonnel> search(String term, String role, UUID directionId) {
        Pageable pageable = PageRequest.of(0, 100);
        return jpaRepository.search(term, role, pageable).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(AgentPersonnel agent) {
        jpaRepository.deleteById(agent.id());
    }
    
    @Override
    public void deleteById(UUID agentId) {
        jpaRepository.deleteById(agentId);
    }
    
    private AgentPersonnelEntity convertirEnEntity(AgentPersonnel agent) {
        RoleEntity roleEntity = convertirRoleEnEntity(agent.role());
        AgentPersonnelEntity entity = new AgentPersonnelEntity(
            agent.id(),
            agent.nom(),
            agent.prenom(),
            agent.email(),
            roleEntity,
            agent.etatAgent()
        );
        return entity;
    }
    
    private AgentPersonnel convertirEnDomain(AgentPersonnelEntity entity) {
        return chargerComplet(entity.getId());
    }
    
    private AgentPersonnel chargerComplet(UUID agentId) {
        AgentPersonnelEntity entity = jpaRepository.findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent non trouvé"));
        
        List<AgentDirection> directions = agentDirectionSpi.findByAgentId(agentId);
        List<AgentAffectation> affectations = affectationSpi.findByAgentId(agentId);
        
        Role role = convertirEntityEnRole(entity.getRole());
        
        return new AgentPersonnel(
            entity.getId(),
            entity.getNom(),
            entity.getPrenom(),
            entity.getEmail(),
            role,
            entity.getEtatAgent(),
            directions,
            affectations,
            null // AgentMaterielEtDroit sera géré séparément si nécessaire
        );
    }
    
    private RoleEntity convertirRoleEnEntity(Role role) {
        return roleJpaRepository.findByValeur(role.value())
            .orElseThrow(() -> new RuntimeException("Role non trouvé avec la valeur: " + role.value()));
    }
    
    private Role convertirEntityEnRole(RoleEntity roleEntity) {
        // Convertir la valeur de RoleEntity vers l'enum Role
        return switch (roleEntity.getValeur()) {
            case 1 -> Role.Agent;
            case 2 -> Role.Manager;
            case 3 -> Role.Admin;
            case 4 -> Role.Prestataire;
            case 5 -> Role.Conseiller;
            default -> throw new RuntimeException("Valeur de rôle inconnue: " + roleEntity.getValeur());
        };
    }
}