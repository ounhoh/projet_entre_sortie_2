package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TacheContenu;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateTacheEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.TemplateTacheJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class TemplateTacheAdapter implements TemplateTacheSpi {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateTacheAdapter.class);
    
    private final TemplateTacheJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;
    
    public TemplateTacheAdapter(TemplateTacheJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public TemplateTache save(TemplateTache tache) {
        TemplateTacheEntity entity = convertirEnEntity(tache);
        TemplateTacheEntity entitySauvegardee = jpaRepository.save(entity);
        return convertirEnDomain(entitySauvegardee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TemplateTache> findById(UUID tacheId) {
        return jpaRepository.findById(tacheId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateTache> findByTemplateGroupeId(UUID groupeId) {
        return jpaRepository.findByTemplateGroupeId(groupeId).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateTache> findAll() {
        return jpaRepository.findAll().stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(TemplateTache templateTache) {
        jpaRepository.deleteById(templateTache.id());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existById(UUID id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByTemplateGroupeId(UUID templateGroupeId) {
        return jpaRepository.countByTemplateGroupeId(templateGroupeId);
    }
    
    private TemplateTacheEntity convertirEnEntity(TemplateTache tache) {
        String contenuJson = convertirContenuEnJson(tache.contenu());
        return new TemplateTacheEntity(
            tache.id(),
            tache.code(),
            tache.libelle(),
            tache.description(),
            tache.type(),
            tache.delaijour(),
            contenuJson,
            tache.dependanceId()
        );
    }
    
    private TemplateTache convertirEnDomain(TemplateTacheEntity entity) {
        logger.debug("Conversion de TemplateTacheEntity en TemplateTache pour code: {}, contenu JSON: {}", 
                     entity.getCode(), entity.getContenu());
        TacheContenu contenu = convertirJsonEnContenu(entity.getContenu());
        logger.debug("Contenu converti - taille: {}", contenu.contenuTache().size());
        return new TemplateTache(
            entity.getId(),
            entity.getCode(),
            entity.getLibelle(),
            entity.getDescription(),
            entity.getType(),
            entity.getDelaiJour(),
            contenu,
            entity.getDependanceId()
        );
    }
    
    private String convertirContenuEnJson(TacheContenu contenu) {
        try {
            if (contenu == null || contenu.contenuTache() == null || contenu.contenuTache().isEmpty()) {
                return "{}";
            }
            // Sérialiser toute la Map pour préserver champs et actions
            return objectMapper.writeValueAsString(contenu.contenuTache());
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private TacheContenu convertirJsonEnContenu(String json) {
        try {
            if (json == null || json.isEmpty() || json.trim().equals("{}") || json.trim().equals("[]")) {
                logger.warn("JSON contenu est null, vide ou vide: {}", json);
                return new TacheContenu(Map.of());
            }
            
            logger.debug("Tentative de parsing JSON comme objet: {}", json.substring(0, Math.min(100, json.length())));
            
            // Essayer de parser comme un objet JSON (format actuel avec champs et actions)
            try {
                Map<String, Object> contenuMap = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                logger.debug("JSON parsé comme objet - clés: {}, taille: {}", contenuMap.keySet(), contenuMap.size());
                // Si c'est un objet avec des clés (comme "champs", "actions"), on le garde tel quel
                if (contenuMap.containsKey("champs") || contenuMap.containsKey("actions") || contenuMap.size() > 0) {
                    return new TacheContenu(contenuMap);
                }
            } catch (Exception e) {
                logger.debug("Échec du parsing comme objet, tentative comme liste: {}", e.getMessage());
                // Si ça échoue, essayer comme une liste (ancien format)
            }
            
            // Fallback : Le JSON stocké est une liste de champs : [{...}, {...}]
            // On la convertit en Map avec la clé "champs"
            try {
                List<Map<String, Object>> champs = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
                logger.debug("JSON parsé comme liste - taille: {}", champs.size());
                Map<String, Object> contenuMap = Map.of("champs", champs);
                return new TacheContenu(contenuMap);
            } catch (Exception e) {
                logger.error("Échec du parsing JSON (objet et liste), retour d'une Map vide. JSON: {}", json, e);
                // Si les deux échouent, retourner une Map vide
                return new TacheContenu(Map.of());
            }
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de la conversion JSON en contenu: {}", json, e);
            return new TacheContenu(Map.of());
        }
    }
}