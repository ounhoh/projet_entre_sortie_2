package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.agent.AgentMaterielSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentMaterielEtDroit;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentMaterielEtDroitEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.AgentMaterielEtDroitJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class AgentMaterielAdapter implements AgentMaterielSpi {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentMaterielAdapter.class);
    
    private final AgentMaterielEtDroitJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;
    
    public AgentMaterielAdapter(AgentMaterielEtDroitJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public AgentMaterielEtDroit save(UUID agentId, AgentMaterielEtDroit materiel) {
        try {
            Map<String, Object> materielMap = materiel.agentMaterileEtDroit();
            
            // Log détaillé avant conversion
            logger.info("=== SAUVEGARDE AgentMaterielEtDroit pour l'agent {} ===", agentId);
            logger.info("Structure complète du Map avant sérialisation:");
            logger.info("  - Nombre de clés: {}", materielMap.size());
            logger.info("  - Clés présentes: {}", materielMap.keySet());
            for (Map.Entry<String, Object> entry : materielMap.entrySet()) {
                logger.info("  - Clé '{}': valeur = {} (type: {})", 
                    entry.getKey(), 
                    entry.getValue(), 
                    entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null");
            }
            
            // Convertir le Map en JSON
            String json = objectMapper.writeValueAsString(materielMap);
            logger.info("JSON généré pour l'agent {}: {}", agentId, json);
            logger.info("Taille du JSON: {} caractères", json.length());
            
            // Vérifier si un enregistrement existe déjà pour cet agent
            Optional<AgentMaterielEtDroitEntity> existing = jpaRepository.findByAgentId(agentId);
            
            AgentMaterielEtDroitEntity entity;
            if (existing.isPresent()) {
                // Mettre à jour l'existant
                entity = existing.get();
                entity.setMaterielEtDroit(json);
            } else {
                // Créer un nouveau
                entity = new AgentMaterielEtDroitEntity(UUID.randomUUID(), agentId, json);
            }
            
            AgentMaterielEtDroitEntity saved = jpaRepository.save(entity);
            logger.info("Entité sauvegardée avec l'ID: {} pour l'agent {}", saved.getId(), agentId);
            
            AgentMaterielEtDroit converted = convertirEnDomain(saved);
            logger.info("=== FIN SAUVEGARDE AgentMaterielEtDroit pour l'agent {} ===", agentId);
            
            return converted;
            
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde du matériel pour l'agent {}: {}", agentId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la sauvegarde du matériel: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AgentMaterielEtDroit> findByAgentId(UUID agentId) {
        return jpaRepository.findByAgentId(agentId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    public void deleteByAgentId(UUID agentId) {
        jpaRepository.findByAgentId(agentId)
            .ifPresent(jpaRepository::delete);
    }
    
    private AgentMaterielEtDroit convertirEnDomain(AgentMaterielEtDroitEntity entity) {
        try {
            if (entity.getMaterielEtDroit() == null || entity.getMaterielEtDroit().isEmpty()) {
                logger.info("Aucune donnée JSON pour l'agent {} (vide ou null)", entity.getAgentId());
                return new AgentMaterielEtDroit(Map.of());
            }
            
            logger.info("=== LECTURE AgentMaterielEtDroit pour l'agent {} ===", entity.getAgentId());
            logger.info("JSON brut depuis la base: {}", entity.getMaterielEtDroit());
            
            Map<String, Object> materielMap = objectMapper.readValue(
                entity.getMaterielEtDroit(),
                new TypeReference<Map<String, Object>>() {}
            );
            
            logger.info("Map convertie depuis JSON pour l'agent {}: {}", entity.getAgentId(), materielMap);
            logger.info("  - Nombre de clés: {}", materielMap.size());
            logger.info("  - Clés présentes: {}", materielMap.keySet());
            for (Map.Entry<String, Object> entry : materielMap.entrySet()) {
                logger.info("  - Clé '{}': valeur = {} (type: {})", 
                    entry.getKey(), 
                    entry.getValue(), 
                    entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null");
            }
            logger.info("=== FIN LECTURE AgentMaterielEtDroit pour l'agent {} ===", entity.getAgentId());
            
            return new AgentMaterielEtDroit(materielMap);
        } catch (Exception e) {
            logger.warn("Erreur lors de la conversion du matériel pour l'agent {}: {}", 
                       entity.getAgentId(), e.getMessage(), e);
            return new AgentMaterielEtDroit(Map.of());
        }
    }
}
