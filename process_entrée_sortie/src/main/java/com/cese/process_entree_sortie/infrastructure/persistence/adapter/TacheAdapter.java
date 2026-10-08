package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.Tache.model.TacheContenu;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceTacheEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.InstanceTacheJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class TacheAdapter implements TacheSpi {
    
    private static final Logger logger = LoggerFactory.getLogger(TacheAdapter.class);
    
    private final InstanceTacheJpaRepository jpaRepository;
    private final GroupeTacheSpi groupeTacheSpi;
    private final ObjectMapper objectMapper;
    
    public TacheAdapter(InstanceTacheJpaRepository jpaRepository, @Lazy GroupeTacheSpi groupeTacheSpi, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.groupeTacheSpi = groupeTacheSpi;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public InstanceTache save(InstanceTache instanceTache) {
        // Charger l'ancienne tâche pour comparer les statuts
        Optional<InstanceTache> ancienneTacheOpt = jpaRepository.findById(instanceTache.id())
            .map(this::convertirEnDomain);
        
        InstanceTacheEntity entity = convertirEnEntity(instanceTache);
        InstanceTacheEntity entitySauvegardee = jpaRepository.save(entity);
        InstanceTache tacheSauvegardee = convertirEnDomain(entitySauvegardee);
        
        // Mettre à jour le groupe si :
        // 1. La tâche a un groupe
        // 2. ET (la tâche est maintenant "fait" OU "à faire" OU le statut a changé)
        if (tacheSauvegardee.groupeTacheId() != null) {
            boolean statutAChange = ancienneTacheOpt
                .map(ancienne -> !ancienne.statut().equals(tacheSauvegardee.statut()))
                .orElse(true); // Si c'est une nouvelle tâche, considérer comme changement
            
            // Mettre à jour le groupe si la tâche est "fait", "à faire", ou si le statut a changé
            if (tacheSauvegardee.statut().equals(StatutTache.fait) || 
                tacheSauvegardee.statut().equals(StatutTache.aFaire) || 
                statutAChange) {
                mettreAJourGroupeSiNecessaire(tacheSauvegardee.groupeTacheId());
            }
        }
        
        return tacheSauvegardee;
    }
    
    /**
     * Vérifie si toutes les tâches du groupe sont faites et met à jour le groupe automatiquement.
     */
    private void mettreAJourGroupeSiNecessaire(UUID groupeId) {
        try {
            Optional<InstanceGroupeTache> groupeOpt = groupeTacheSpi.findById(groupeId);
            if (groupeOpt.isPresent()) {
                InstanceGroupeTache groupe = groupeOpt.get();
                StatutTache ancienStatut = groupe.statut();
                // Vérifier si toutes les tâches sont faites et mettre à jour le groupe
                InstanceGroupeTache groupeModifie = groupe.changerStatut();
                if (!groupeModifie.statut().equals(ancienStatut)) {
                    groupeTacheSpi.save(groupeModifie);
                    logger.info("Groupe {} mis à jour automatiquement du statut {} au statut {}", 
                        groupeId, ancienStatut, groupeModifie.statut());
                } else {
                    logger.debug("Groupe {} : statut inchangé ({})", groupeId, ancienStatut);
                }
            }
        } catch (Exception e) {
            // Ne pas faire échouer la sauvegarde de la tâche si la mise à jour du groupe échoue
            logger.warn("Erreur lors de la mise à jour automatique du groupe {}: {}", groupeId, e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstanceTache> findById(UUID tacheId) {
        return jpaRepository.findById(tacheId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceTache> findbyProcessusId(UUID processusId) {
        return jpaRepository.findByProcessusId(processusId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceTache> findByAgentId(UUID agentId) {
        // Récupérer les groupes assignés à l'agent, puis toutes les tâches de ces groupes
        List<InstanceGroupeTache> groupes = groupeTacheSpi.findByAgentId(agentId);
        List<UUID> groupeIds = groupes.stream()
            .map(InstanceGroupeTache::id)
            .collect(Collectors.toList());
        return jpaRepository.findByGroupeTacheIdIn(groupeIds).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceTache> findByStatut(StatutTache statutTache) {
        return jpaRepository.findByStatut(statutTache).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceTache> findEnRetard() {
        return jpaRepository.findEnRetard(LocalDate.now()).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceTache> findByGroupeId(UUID groupeId) {
        return jpaRepository.findByGroupeTacheId(groupeId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(InstanceTache instanceTache) {
        jpaRepository.deleteById(instanceTache.id());
    }
    
    private InstanceTacheEntity convertirEnEntity(InstanceTache tache) {
        String contenuJson = convertirContenuEnJson(tache.contenu());
        return new InstanceTacheEntity(
            tache.id(),
            tache.code(),
            tache.libelle(),
            contenuJson,
            tache.statut(),
            tache.dateEcheance(),
            tache.templateId(),
            tache.groupeTacheId(),
            tache.dependanceId()
        );
    }
    
    private InstanceTache convertirEnDomain(InstanceTacheEntity entity) {
        TacheContenu contenu = convertirJsonEnContenu(entity.getContenu());
        return new InstanceTache(
            entity.getId(),
            entity.getCode(),
            entity.getLibelle(),
            contenu,
            entity.getStatut(),
            entity.getDateEcheance(),
            entity.getTemplateId(),
            entity.getGroupeTacheId(),
            entity.getDependanceId()
        );
    }
    
    private String convertirContenuEnJson(TacheContenu contenu) {
        try {
            if (contenu == null || contenu.contenuTache() == null || contenu.contenuTache().isEmpty()) {
                return "{}";
            }
            return objectMapper.writeValueAsString(contenu.contenuTache());
        } catch (Exception e) {
            logger.warn("Erreur lors de la sérialisation du contenu de la tâche: {}", e.getMessage());
            return "{}";
        }
    }
    
    private TacheContenu convertirJsonEnContenu(String json) {
        try {
            if (json == null || json.isEmpty() || json.trim().equals("{}") || json.trim().equals("[]")) {
                return new TacheContenu(Map.of());
            }
            Map<String, Object> contenuMap = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            return new TacheContenu(contenuMap);
        } catch (Exception e) {
            logger.warn("Erreur lors de la désérialisation du contenu de la tâche: {}", e.getMessage());
            return new TacheContenu(Map.of());
        }
    }
}